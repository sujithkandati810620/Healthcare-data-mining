import streamlit as st
import requests

# Define Flask server URLs
BASE_URL = "http://127.0.0.1:5000" 
DISEASE_NAMES_URL = f"{BASE_URL}/diseasenames"
SYMPTOM_NAMES_URL = f"{BASE_URL}/allsymptoms"

def fetch_disease_information(disease_name):
    api_url = f"{BASE_URL}/get_symptoms_and_treatments_and_posts/{disease_name}"

    try:
        response = requests.get(api_url)
        if response.status_code == 200:
            data = response.json()

            # Display symptoms
            st.write("<h3 style='color:red;'>Symptoms</h3>", unsafe_allow_html=True)
            with st.expander("All Symptoms"):
                if data['symptoms']:
                    for symptom in data['symptoms']:
                        st.write(f"**{symptom['description']}**")
                else:
                    st.write("No Symptoms for this disease")

            # Display treatments
            st.write("<h3 style='color:red;'>Treatments</h3>", unsafe_allow_html=True)
            with st.expander("All Treatments"):
                if data['treatments']:
                    for treatment in data['treatments']:
                        st.write(f"**{treatment['description']}**")
                else:
                    st.write("No Treatments for this disease")

            # Display posts
            st.write("<h3 style='color:red;'>Posts</h3>", unsafe_allow_html=True)
            with st.expander("All posts"):
                if data['posts']:
                    for post in data['posts']:
                        st.write(f"**{post['links']}**")
                else:
                    st.write("No Posts for this disease")
        else:
            st.error(f"Failed to fetch information. Status code: {response.status_code}")
    except Exception as e:
        st.error(f"An error occurred: {e}")

def fetch_symptom_information(symptom_name):
    api_url = f"{BASE_URL}/get_diseases_and_percentages_for_symptom/{symptom_name}"

    try:
        response = requests.get(api_url)
        if response.status_code == 200:
            data = response.json()

            st.write("<h3 style='color:red;'>Relavant diseases</h3>", unsafe_allow_html=True)
            with st.expander("Diseases"):
                if data['diseases']:
                    for disease in data['diseases']:
                        percentage = disease['percentage']
                        disease_name = disease['disease_name']
                        st.write(f"**{disease_name} - <span style='color: brown;'>{percentage}%</span>**",
                                 unsafe_allow_html=True)
                else:
                    st.write("No relevant diseases for this symptom")
        else:
            st.error(f"Failed to fetch information. Status code: {response.status_code}")
    except Exception as e:
        st.error(f"An error occurred: {e}")

def main():
    # Fetch all diseases and symptoms for input suggestions
    response = requests.get(DISEASE_NAMES_URL)
    all_diseases = sorted(response.json().get('diseases_names', []))

    response = requests.get(SYMPTOM_NAMES_URL)
    all_symptoms = sorted(response.json().get('symptom_names', []))

    st.title("Disease and Symptom Information")

    category = st.radio("Select a category:", ["Disease", "Symptom"])

    if category == "Disease":
        disease_input = st.text_input("Enter a disease:")
        filtered_diseases = [disease for disease in all_diseases if disease.lower().startswith(disease_input.lower())]
        selected_disease = st.selectbox("Select a disease:", filtered_diseases)

        if st.button("Get Information"):
            if selected_disease:
                fetch_disease_information(selected_disease)
            else:
                st.warning("Please select a disease.")

    elif category == "Symptom":
        symptom_input = st.text_input("Enter a symptom:")
        filtered_symptoms = [symptom for symptom in all_symptoms if symptom_input.lower() in symptom.lower()]
        selected_symptom = st.selectbox("Select a symptom:", filtered_symptoms)

        if st.button("Get Information"):
            if selected_symptom:
                fetch_symptom_information(selected_symptom)
            else:
                st.warning("Please select a symptom.")

if __name__ == "__main__":
    main()
