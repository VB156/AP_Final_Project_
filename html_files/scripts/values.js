/**
 * This function sends values to the graphFrame for updating the graph.
 * It uses the postMessage API to send a message to the iframe.
 *
 * @param {Array} values - The array of values to be sent to the graphFrame.
 */
function sendValuesToGraph(values) {
    // Get the reference to the graphFrame iframe
    const graphFrame = window.parent.document.getElementById('graphFrame');
    // Send a message to the graphFrame
    graphFrame.contentWindow.postMessage({type: 'updateValues', values: values}, '*');
}