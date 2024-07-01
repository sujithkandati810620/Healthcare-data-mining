# SympGraph Generator

## Overview

SympGraph Generator is a Python tool for creating symptom graphs from a dataset. The tool reads a CSV file (combinedData.csv) containing diseases, symptoms, treatments data, processes it, and generates a graph representation of the connections and weights between different symptoms.

## Files in the Repository

`sympgraph_generator.py`: This is the main Python script that reads the `combinedData.csv` file generated from Metamap, processes the symptom data, and generates a graph using libraries such as NumPy, Matplotlib, and NetworkX.

`requirements.txt`: Lists the Python libraries required to run the script (numpy, matplotlib, networkx).

## Installation and Setup

1. **Clone the Repository:** Clone this repository to your local machine.

2. **Install Dependencies:** Run the following command to install the necessary Python libraries:

`pip install -r requirements.txt`

## Usage

1. **Prepare Your Data:** Ensure your metamap combined data is in the `../metamap/data/` folder.

2. **Run the Script:** Execute `sympgraph_generator.py` to generate the symptom graph. The script will read the data from above mentioned file and generate `sympgraph.csv` file as consisting of the final symgraph output comprising of data  in the format `Source,Destination,Weight` and a graph visualization of a sample of first 20 posts.