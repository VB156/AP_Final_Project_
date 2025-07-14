/**
 Calculates the coordinates for placing nodes in a circular layout.
 @param {number} n - The number of nodes to place in the circle.
 @param {number} width - The width of the container where the nodes will be placed.
 @param {number} height - The height of the container where the nodes will be placed.
 @returns {Array} An array of objects, each containing the x and y coordinates for a node.
 */
function getCircleCoordinates(n, width, height) {
    const coordinates = [];
    const angleStep = (2 * Math.PI) / n;
    const radius = Math.min(width, height) / 2 - 20;

    for (let i = 0; i < n; i++) {
        const angle = i * angleStep;
        const x = radius * Math.cos(angle) + width / 2;
        const y = radius * Math.sin(angle) + height / 2;
        coordinates.push({x: x, y: y});
    }

    return coordinates;
}

/**
 This function calculates the width and height of an HTML element with a given class name.
 @param {string} className - The class name of the HTML element whose size needs to be calculated.
 @returns {Object} An object containing the width and height of the HTML element.
 */
function getElementSizeByClass(className) {
    // Create a temporary element
    const tempElement = document.createElement('div');
    // Add the class to the temporary element
    tempElement.className = className;
    // Append the temporary element to the body (off-screen)
    tempElement.style.position = 'absolute';
    tempElement.style.visibility = 'hidden';
    document.body.appendChild(tempElement);
    // Get the computed style of the temporary element
    const computedStyle = window.getComputedStyle(tempElement);
    const width = parseInt(computedStyle.width, 10);
    const height = parseInt(computedStyle.height, 10);
    // Remove the temporary element from the DOM
    document.body.removeChild(tempElement);
    return {"width": width, "height": height};
}

const agentRadius = 45; // Must match the 'r' value in graph.css
const topicSize = getElementSizeByClass('topic'); // Gets {width, height} from CSS
const sideOffset = 110; // How far to the side inputs/outputs are placed

/**
 * Calculates the intersection point of a line and a node's shape.
 * This ensures the link line connects to the edge of the node, not its center.
 * @param {Object} sourceNode - The source node of the link.
 * @param {Object} targetNode - The target node of the link.
 * @returns {Object} An object with x and y coordinates for the intersection point.
 */
function calculateIntersectionPoint(sourceNode, targetNode) {
    const dx = targetNode.x - sourceNode.x;
    const dy = targetNode.y - sourceNode.y;
    const dist = Math.sqrt(dx * dx + dy * dy);

    let x, y;

    if (targetNode.type === 'Agent') {
        x = targetNode.x - (dx * agentRadius) / dist;
        y = targetNode.y - (dy * agentRadius) / dist;
    } else { // It's a Topic
        // Get the actual size of this specific topic node from its <rect> element
        const topicRect = d3.select(targetNode.nodeRef).select('rect');
        const halfW = (topicRect.empty() ? topicSize.width : topicRect.attr('width')) / 2;
        const halfH = (topicRect.empty() ? topicSize.height : topicRect.attr('height')) / 2;

        const absDx = Math.abs(dx);
        const absDy = Math.abs(dy);
        let overlap;

        if (absDy === 0 && absDx === 0) { // Should not happen in a simulation
            overlap = 0;
        } else if (absDy * halfW > absDx * halfH) {
             overlap = halfH / absDy;
        } else {
             overlap = halfW / absDx;
        }
        x = targetNode.x - overlap * dx;
        y = targetNode.y - overlap * dy;
    }
    return { x, y };
}

/**
 Calculates the end coordinates of a link based on the source and target nodes.
 @param {Object} source - The source node object with properties: x, y, and type.
 @param {Object} target - The target node object with properties: x, y, and type.
 @param {boolean} isX - A flag indicating whether to calculate the x-coordinate (true) or y-coordinate (false).
 @returns {number} The calculated x or y coordinate of the link end.
 */
function calculateLinkEnd(source, target, isX) {
    const dx = target.x - source.x;
    const dy = target.y - source.y;
    const angle = Math.atan2(dy, dx);

    if (source.type === 'Topic') {
        const offset = size.height / 2;
        return isX ? source.x + Math.cos(angle) * offset : source.y + Math.sin(angle) * offset;
    } else if (source.type === 'Agent') {
        const radius = 15;
        return isX ? source.x + Math.sin(angle) * radius : source.y + Math.cos(angle) * radius;
    }
}

function renderGraph() {
    var iframe = window.frameElement;
    const width = iframe ? iframe.offsetWidth - 20 : window.innerWidth - 20;
    const height = iframe ? iframe.offsetHeight - 20 : window.innerHeight - 20;

    // Re-create nodes and links from graphData each time
    const nodes = graphData.map(node => ({
        id: node.id,
        name: node.name,
        type: node.type,
        value: ""
    }));

    const links = [];
    graphData.forEach(node => {
        node.edges.forEach(edge => {
            links.push({source: node.id, target: edge});
        });
    });

    // Remove old SVG content
    d3.select("#svgID").selectAll("*").remove();

    // Set SVG size and viewBox
    const svg = d3.select("#svgID")
        .attr("width", "100%")
        .attr("height", "100%")
        .attr("viewBox", `0 0 ${width} ${height}`);

    // --- New Layout Logic ---
    // (Copy your existing graph rendering code here, replacing all uses of 'width' and 'height' with these new values)

    // 1. Process nodes and links to establish input/output relationships
    const nodeMap = new Map(nodes.map(n => [n.id, n]));
    nodes.forEach(n => {
        n.connections = []; // Store connections for topics
    });
    links.forEach(link => {
        const source = nodeMap.get(link.source);
        const target = nodeMap.get(link.target);
        if (source.type === 'Topic' && target.type === 'Agent') {
            source.connections.push({ agent: target, type: 'input' });
        } else if (source.type === 'Agent' && target.type === 'Topic') {
            target.connections.push({ agent: source, type: 'output' });
        }
    });

    // 2. Calculate target positions for the agents in a large circle
    const agents = nodes.filter(n => n.type === 'Agent');
    const numAgents = agents.length;
    if (numAgents > 0) {
        const groupRadius = Math.min(width, height) / 2.3;
        const groupAngleStep = (2 * Math.PI) / numAgents;
        agents.forEach((agent, i) => {
            const angle = i * groupAngleStep;
            agent.targetX = groupRadius * Math.cos(angle) + width / 2;
            agent.targetY = groupRadius * Math.sin(angle) + height / 2;
        });
    }

    /**
     * Calculates the target X coordinate for a given node based on its role.
     * @param {Object} d - The node data.
     * @returns {number} The target X coordinate.
     */
    function getTargetX(d, width, height) {
        if (d.type === 'Agent') {
            return d.targetX;
        }
        if (d.type === 'Topic' && d.connections.length > 0) {
            let totalX = 0;
            d.connections.forEach(conn => {
                const agent = conn.agent;
                const vx = agent.targetX - width / 2;
                const vy = agent.targetY - height / 2;
                const mag = Math.sqrt(vx * vx + vy * vy);
                if (mag === 0) { totalX += agent.targetX; return; }
                const px = -vy / mag;
                const offset = conn.type === 'input' ? sideOffset : -sideOffset;
                totalX += agent.targetX + px * offset;
            });
            return totalX / d.connections.length;
        }
        return width / 2;
    }

    /**
     * Calculates the target Y coordinate for a given node based on its role.
     * @param {Object} d - The node data.
     * @returns {number} The target Y coordinate.
     */
    function getTargetY(d, width, height) {
        if (d.type === 'Agent') {
            return d.targetY;
        }
        if (d.type === 'Topic' && d.connections.length > 0) {
            let totalY = 0;
            d.connections.forEach(conn => {
                const agent = conn.agent;
                const vx = agent.targetX - width / 2;
                const vy = agent.targetY - height / 2;
                const mag = Math.sqrt(vx * vx + vy * vy);
                if (mag === 0) { totalY += agent.targetY; return; }
                const py = vx / mag;
                const offset = conn.type === 'input' ? sideOffset : -sideOffset;
                totalY += agent.targetY + py * offset;
            });
            return totalY / d.connections.length;
        }
        return height / 2;
    }

    // --- End of New Layout Logic ---

    const simulation = d3.forceSimulation(nodes)
        .force("link", d3.forceLink(links).id(d => d.id).distance(200).strength(1.2))
        .force("charge", d3.forceManyBody().strength(-800))
        .force("collide", d3.forceCollide(65).strength(1))
        .force("x", d3.forceX(d => getTargetX(d, width, height)).strength(0.08))
        .force("y", d3.forceY(d => getTargetY(d, width, height)).strength(0.08));

    const textPadding = { x: 10, y: 8 };

    const link = svg.append("g")
        .attr("class", "links")
        .selectAll("line")
        .data(links)
        .enter().append("line")
        .attr("class", "link");

    const node = svg.append("g")
        .attr("class", "nodes")
        .selectAll("g")
        .data(nodes)
        .enter().append("g")
        .attr("class", "node")
        .each(function(d) { d.nodeRef = this; }) // Save DOM reference for size calculation
        .call(d3.drag()
            .on("start", dragstarted)
            .on("drag", dragged)
            .on("end", dragended));

    node.each(function(d) {
        const group = d3.select(this);

        if (d.type === 'Agent') {
            group.append("circle")
                .attr("class", "agent");
            group.append("text")
                .attr("dy", 5)
                .attr("class", "agent-text")
                .text(d.name);
        } else if (d.type === 'Topic') {
            const text = group.append("text")
                .attr("dy", 5)
                .attr("class", "topic-text")
                .text(d.name);

            const bbox = text.node().getBBox();

            group.insert("rect", "text")
                .attr("class", "topic")
                .attr("x", bbox.x - textPadding.x)
                .attr("y", bbox.y - textPadding.y)
                .attr("width", bbox.width + 2 * textPadding.x)
                .attr("height", bbox.height + 2 * textPadding.y);
        }
    });

    simulation.on("tick", () => {
        link
            .attr("x1", d => d.source.x)
            .attr("y1", d => d.source.y)
            .attr("x2", d => calculateIntersectionPoint(d.source, d.target).x)
            .attr("y2", d => calculateIntersectionPoint(d.source, d.target).y);

        // Enforce boundary constraints to keep nodes within the SVG area
        node.each(d => {
            let nodeWidth, nodeHeight;

            if (d.type === 'Agent') {
                nodeWidth = nodeHeight = agentRadius * 2;
            } else { // For Topics, get the current size of the rectangle
                const rect = d3.select(d.nodeRef).select('rect');
                nodeWidth = rect.empty() ? 0 : parseFloat(rect.attr('width'));
                nodeHeight = rect.empty() ? 0 : parseFloat(rect.attr('height'));
            }

            d.x = Math.max(nodeWidth / 2, Math.min(width - nodeWidth / 2, d.x));
            d.y = Math.max(nodeHeight / 2, Math.min(height - nodeHeight / 2, d.y));
        });

        node
            .attr("transform", d => `translate(${d.x},${d.y})`);
    });

    svg.append("defs").selectAll("marker")
        .data(["end"])
        .enter().append("marker")
        .attr("id", "end")
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", 25)
        .attr("refY", 0)
        .attr("markerWidth", 6)
        .attr("markerHeight", 6)
        .attr("orient", "auto")
        .append("path")
        .attr("d", "M0,-5L10,0L0,5")
        .attr("fill", "white");

    link.attr("marker-end", "url(#end)");

    /**
     Handles the start of a drag event for a node.
     */
    function dragstarted(event, d) {
        if (!event.active) simulation.alphaTarget(0.3).restart();
        d.fx = d.x;
        d.fy = d.y;
    }

    /**
     Handles the dragging of a node in the graph.
     */
    function dragged(event, d) {
        d.fx = event.x;
        d.fy = event.y;
    }

    /**
     Handles the end of a drag event for a node.
     */
    function dragended(event, d) {
        if (!event.active) simulation.alphaTarget(0);
        d.fx = null;
        d.fy = null;
    }

    /**
     Handle messages from the parent window
     Used to update node values
     */
    window.addEventListener('message', function (event) {
        if (event.data.type === 'updateValues') {
            const updatedValues = event.data.values;
            // Update node values and text
            nodes.forEach(node => {
                if (updatedValues[node.id] !== undefined) {
                    node.value = updatedValues[node.id];
                }
            });

            node.each(function (d) {
                if (d.type === 'Topic') {
                    const textElement = d3.select(this).select("text");
                    const rectElement = d3.select(this).select("rect");
                    const newText = d.value ? `${d.name}: ${d.value}` : d.name;

                    textElement.text(newText);

                    // Recalculate size and update the rectangle
                    const bbox = textElement.node().getBBox();
                    rectElement
                        .transition().duration(200) // Animate the size change
                        .attr("x", bbox.x - textPadding.x)
                        .attr("y", bbox.y - textPadding.y)
                        .attr("width", bbox.width + 2 * textPadding.x)
                        .attr("height", bbox.height + 2 * textPadding.y);
                }
            });
        }
    });

    /**
     * Populates the topic dropdown in the form with the available topics from the graph.
     */
    function updateTopicDropdown() {
        const topicNames = [...new Set(nodes.filter(n => n.type === 'Topic').map(n => n.name))];
        topicNames.sort();

        const formIframe = window.parent.document.querySelector('.inputForms');
        if (formIframe) {
            const formDoc = formIframe.contentWindow.document;
            const topicSelect = formDoc.getElementById('topic');

            if (topicSelect) {
                // Clear existing options except the placeholder
                topicSelect.innerHTML = '<option value="" disabled selected>-- Select a Topic --</option>';
                
                // Add new options
                topicNames.forEach(name => {
                    const option = document.createElement('option');
                    option.value = name;
                    option.textContent = name;
                    topicSelect.appendChild(option);
                });
            }
        }
    }

    // Initial population of the dropdown when the script runs
    updateTopicDropdown();
}

// Initial render
renderGraph();

// Re-render on resize
window.addEventListener('resize', renderGraph);