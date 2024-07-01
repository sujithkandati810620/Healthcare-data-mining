from flask import Flask, jsonify
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import text

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql://root:test@localhost/ontology'  # Replace with your actual database URI
db = SQLAlchemy(app)

# Model for diseases table
class Diseases(db.Model):
    disease_id = db.Column(db.String(255), primary_key=True)
    disease_name = db.Column(db.String(255))
    description = db.Column(db.String(255))
    records_count = db.Column(db.Integer)
    has_symptom_count = db.Column(db.Integer)
    has_treatment_count = db.Column(db.Integer)

# Model for treatment table
class Treatment(db.Model):
    treatment_id = db.Column(db.String(255), primary_key=True)
    description = db.Column(db.String(255))
    type = db.Column(db.String(255))

# Model for symptoms table
class Symptoms(db.Model):
    symptom_id = db.Column(db.String(255), primary_key=True)
    description = db.Column(db.String(255))

# Model for disease_symptoms table
class DiseaseSymptom(db.Model):
    disease_id = db.Column(db.String(255), primary_key=True)
    symptom_id = db.Column(db.String(255), primary_key=True)
    count = db.Column(db.Integer)

# Model for disease_treatments table
class DiseaseTreatment(db.Model):
    disease_id = db.Column(db.String(255), primary_key=True)
    treatment_id = db.Column(db.String(255), primary_key=True)
    count = db.Column(db.Integer)

# Model for disease_posts table
class DiseasePost(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    disease_name = db.Column(db.String(255))
    post_link = db.Column(db.String(255))

# Routes for Diseases
@app.route('/diseasenames')
def get_disease_names():
    disease_names = [disease.disease_name for disease in Diseases.query.all()]
    return jsonify({'diseases_names': disease_names})

def get_symptoms_and_treatments_for_disease(disease_id):
    session = db.session()
    query = text(f"SELECT s.* FROM symptoms s "
                 f"INNER JOIN disease_symptoms ds ON s.symptom_id = ds.symptom_id "
                 f"WHERE ds.disease_id = :diseaseId")
    symptoms = session.execute(query, {'diseaseId': disease_id}).fetchall()

    symptoms_list = [{'symptom_id': symptom.symptom_id, 'description': symptom.description}
                     for symptom in symptoms]
    
    query = text(f"SELECT t.* FROM treatment t "
                 f"INNER JOIN disease_treatments dt ON t.treatment_id = dt.treatment_id "
                 f"WHERE dt.disease_id = :diseaseId")
    treatments = session.execute(query, {'diseaseId': disease_id}).fetchall()

    treatments_list = [{'treatment_id': treatment.treatment_id, 'description': treatment.description, 'type': treatment.type}
                       for treatment in treatments]

    session.close()

    return symptoms_list, treatments_list

@app.route('/get_symptoms_and_treatments_and_posts/<disease_name>')
def get_symptoms_and_treatments(disease_name):
    disease_id = None
    result = {}
    try:
        disease_id = Diseases.query.filter_by(disease_name=disease_name).first().disease_id
        symptoms, treatments = get_symptoms_and_treatments_for_disease(disease_id)
        posts = get_posts(disease_name)
        result = {'symptoms': symptoms, 'treatments': treatments, 'posts': posts}
        return jsonify(result)
    except Exception as e:
        return jsonify(result)

def get_posts(disease_name):
    session = db.session()
    query = text(f"SELECT dp.post_link from disease_posts dp " 
                 f"WHERE dp.disease_name = :diseasename")
    posts = session.execute(query, {'diseasename': disease_name}).fetchall()
    posts_list = [{'links': post.post_link} for post in posts]
    session.close()
    return posts_list
    
# Routes for Symptoms
@app.route('/allsymptoms')
def get_symptom_names():
    symptom_names = [symptom.description for symptom in Symptoms.query.all()]
    return jsonify({'symptom_names': symptom_names})

def get_diseases_and_percentages_for_symptom(symptom_description):
    session = db.session()
    query = text(f"SELECT d.disease_name, ds.count, ds.count * 100.0 / sum(ds.count) over () as disease_percent "
                 f"FROM diseases d "
                 f"INNER JOIN disease_symptoms ds ON d.disease_id = ds.disease_id "
                 f"INNER JOIN symptoms s ON ds.symptom_id = s.symptom_id "
                 f"WHERE s.description LIKE :symptomDescription order by disease_percent desc")
    diseases = session.execute(query, {'symptomDescription': symptom_description}).fetchall()
    diseases_list = [{'disease_name': disease.disease_name, 'percentage': disease.disease_percent}
                     for disease in diseases]
    session.close()
    return diseases_list

@app.route('/get_diseases_and_percentages_for_symptom/<symptom_description>')
def get_diseases_and_percentages(symptom_description):
    diseases = get_diseases_and_percentages_for_symptom(symptom_description)
    result = {'diseases': diseases}
    return jsonify(result)

@app.route('/getDiseasesSymptom/<symptomName>', methods=['GET'])
def get_diseases(symptomName):
    query = text(f"SELECT d.* FROM diseases d "
                 f"INNER JOIN disease_symptoms ds ON d.disease_id=ds.disease_id "
                 f"WHERE ds.symptom_id IN (SELECT id FROM symptom WHERE name LIKE :symptomName)")
    diseases = db.engine.execute(query, symptomName=f"%{symptomName}%").fetchall()

    query = text(f"SELECT ds.count FROM disease d "
                 f"INNER JOIN disease_symptoms ds ON d.disease_id=ds.disease_id "
                 f"WHERE ds.symptom_id IN (SELECT id FROM symptom WHERE name LIKE :symptomName)")
    count_values = db.engine.execute(query, symptomName=f"%{symptomName}%").fetchall()

    disease_counts = [{'disease': {'id': disease.id, 'name': disease.name}, 'count': count}
                      for disease, count in zip(diseases, count_values)]

    return jsonify(disease_counts)

if __name__ == '__main__':
    app.run(debug=True)
