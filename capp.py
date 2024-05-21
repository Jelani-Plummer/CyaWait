from flask import Flask, request, jsonify
import pickle
import pandas as pd
import numpy as np

app = Flask(__name__)

# Load models
def load_model(path):
    with open(path, 'rb') as file:
        return pickle.load(file)

model_1 = load_model('model_1.pkl')
model_2 = load_model('model_2.pkl')
model_3 = load_model('finmod.pkl')

# Define the features expected by each model
features_1 = ['Day of week', 'Arrival Time (minutes)', 'Location score', 'Restaurant type_Call in', 'Restaurant type_Dine in', 'Restaurant type_Fast food', 'Restaurant type_Fifo']
features_2 = ['Restaurant type_Fast food', 'Food type_Chicken', 'Food type_Other', 'Food type_burger', 'Food type_main course', 'Food type_meal', 'Food type_sandwhich']
features_3 = ['Predicted No. of people in front', 'Predicted Queue Time (minutes)']

@app.route("/")
def home():
    return "Hello World!"

@app.route('/predict', methods=['POST'])
def predict():
    input_data = request.get_json()
    input_df = pd.DataFrame([input_data])

    # Map categorical data to numerical data
    day_of_week_mapping = {'Monday': 1, 'Tuesday': 2, 'Wednesday': 3, 'Thursday': 4, 'Friday': 5, 'Saturday': 6, 'Sunday': 7}
    location_score_mapping = {'lightly populated': 1, 'populated': 2, 'heavily populated': 3}

    input_df['Day of week'] = input_df['Day of week'].map(day_of_week_mapping).fillna(0)
    input_df['Location score'] = input_df['Location score'].map(location_score_mapping).fillna(0)

    # Convert time to minutes since midnight
    input_df['Arrival Time (minutes)'] = input_df['Arrival time'].apply(lambda x: int(x.split(':')[0]) * 60 + int(x.split(':')[1]) if isinstance(x, str) and x.count(':') == 2 else np.nan).fillna(0)

    # One-hot encode categorical data
    input_df = pd.get_dummies(input_df, columns=['Food type', 'Restaurant type'])

    # Debug: Print the DataFrame columns before making predictions
    print("DataFrame columns:", input_df.columns)

    # Ensure all required features for models are present and fill missing with 0
    for feature in features_1 + features_2 + features_3:
        if feature not in input_df.columns:
            input_df[feature] = 0

    # Predict using models
    number_of_people_in_front = model_1.predict(input_df[features_1])
    average_time_in_queue = model_2.predict(input_df[features_2])
    input_df['Predicted No. of people in front'] = number_of_people_in_front
    input_df['Predicted Queue Time (minutes)'] = average_time_in_queue

    # Predict final wait time using model_3
    if all(feature in input_df.columns for feature in features_3):
        predicted_wait_times = model_3.predict(input_df[features_3])
    else:
        return jsonify({'error': 'Missing features for final prediction'}), 400

    return jsonify({
        'people_in_front': int(number_of_people_in_front[0]),
        'queue_time': int(average_time_in_queue[0]),
        'total_wait_time': int(predicted_wait_times[0])
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
