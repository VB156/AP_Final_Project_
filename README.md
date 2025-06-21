# Computational Graph System

## Background

This project implements a publisher/subscriber computational graph system for Advanced Programming course. The system allows creating and executing computational graphs where nodes represent mathematical operations (agents) and edges represent data flow between them.

The project consists of:
- **Model Layer**: Publisher/subscriber pattern with computational agents (arithmetic operations,   increment/decrement, power, square root)
- **Controller Layer**: Custom HTTP server with servlet framework for handling requests
- **View Layer**: Web-based interface with real-time graph visualization using D3.js

Key features include:
- Real-time graph execution with parallel agent processing
- Cycle detection to prevent infinite loops
- Dynamic graph configuration loading from text files
- Interactive web interface with three-panel layout
- Topic-based communication between agents

## Installation

### Prerequisites
- Java JDK 8 or higher
- Modern web browser with JavaScript enabled

### Setup
1. Clone or download the project to your local machine
2. Ensure Java is installed and accessible from command line
3. Navigate to the project root directory

## Run Commands

### Compile the Project
```bash
javac -d . project_biu/*.java project_biu/*/*.java
```

### Start the Server
```bash
java Main
```

### Access the Application
1. Open your web browser
2. Navigate to: `http://localhost:1234/app/`
3. The application will display a three-panel interface:
   - Left panel: Configuration form for uploading graph files
   - Center panel: Interactive graph visualization
   - Right panel: Real-time topic values display

### Using the Application
1. Click "Choose File" to upload a `.conf` configuration file
2. The graph will automatically render in the center panel
3. Click on topic nodes to populate the topic name field
4. Monitor real-time values in the right panel
5. To stop the server, type 'x' and press Enter in the terminal

### Sample Configuration Files
The project includes several sample configurations in the `config_files/` directory:
- `simple.conf` - Basic arithmetic operations
- `cycle.conf` - Graph with cycle detection
- `image_graph.conf` - Complex computational graph
- `check.conf` - Validation test graph

### Configuration Format
```
graph.AgentClassName
input_topic1,input_topic2
output_topic
```

Example:
```
graph.PlusAgent
A,B
C
graph.IncAgent
C
D
```
