# Backend and Frontend part of Application

# Flask and Streamlit Application

## Table of Contents

- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Usage](#usage)
- [Database Configuration](#DatabaseConfiguration)
- [Running the Application](#running-the-application)
- [Project Structure](#project-structure)
- [Models](#models)
- [Routes](#routes)
- [License](#license)

## Getting Started

This README file provides information and guidance on the usage and structure of the provided Flask application. The application is designed to interact with a MySQL database named "ontology" and includes models and routes related to diseases, treatments, symptoms, and posts.

### Prerequisites

Make sure you have the following installed on your machine:

- Python (version >= 3.6)
- [pip](https://pip.pypa.io/en/stable/installation/)

### Installation

1. Clone the repository:

    ```bash
    git clone https://github.com/koushikayila/healthcare-data-mining/
    ```

2. Navigate to the Application project directory:

    ```bash
    cd Application
    ```

3. Install dependencies:

    ```bash
    pip install -r requirements.txt
    ```

## DatabaseConfiguration

Update the database connection URI in the following line of the code with your actual database URI

### Running the Application

1. Start the Flask backend:

    ```bash
    python -m backend.py
    ```

   This will run the Flask server.

2. Open another terminal and start the Streamlit app:

    ```bash
    streamlit run app.py
    ```

   This will launch the Streamlit app.

3. Open your web browser and go to [http://localhost:5000](http://localhost:5000) to access the Flask application and [http://localhost:8501](http://localhost:8501) for the Streamlit application.

## Project Structure

Explain the organization of your project, e.g.,

- `backend.py`: Flask application file.
- `app.py`: Streamlit application file.
- `requirements.txt`: List of Python dependencies.

## Models

Diseases: Represents information about diseases.
Treatment: Represents information about treatments.
Symptoms: Represents information about symptoms.
DiseaseSymptom: Represents the relationship between diseases and symptoms, including the count.
DiseaseTreatment: Represents the relationship between diseases and treatments, including the count.
DiseasePost: Represents posts related to diseases.

## Routes

The application includes various routes to retrieve information from the database:

/diseasenames: Retrieves a list of all disease names.
/get_symptoms_and_treatments_and_posts/<disease_name>: Retrieves symptoms, treatments, and posts for a specific disease.
/allsymptoms: Retrieves a list of all symptom names.
/get_diseases_and_percentages_for_symptom/<symptom_description>: Retrieves diseases and their percentages for a given symptom.
/getDiseasesSymptom/<symptomName>: Retrieves diseases and their counts for a given symptom.
