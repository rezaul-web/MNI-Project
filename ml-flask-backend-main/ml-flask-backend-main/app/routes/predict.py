from flask import request, jsonify
import tensorflow as tf
import numpy as np
from PIL import Image
import io
import logging

logger = logging.getLogger(__name__)

# Log when the module is loaded
logger.info("Loading melanoma detection model...")
model = tf.keras.models.load_model('melanoma_nevus_model.h5')
logger.info("Model loaded successfully")

# Configuration (must match training)
ANATOM_SITE_CATEGORIES = ['head/neck', 'upper extremity', 'lower extremity',
                          'torso', 'palms/soles', 'oral/genital']
IMG_SIZE = (224, 224)
AGE_MIN = 10  # Replace with your actual min age from training
AGE_MAX = 90  # Replace with your actual max age from training

def interpret_prediction(pred):
    mel_prob, nv_prob = pred
    if mel_prob > 0.7:
        return "High probability of melanoma - Consult a dermatologist immediately"
    elif mel_prob > 0.4:
        return "Moderate probability of melanoma - Recommended to see a specialist"
    else:
        return "Low probability of melanoma - Likely benign nevus"

def preprocess_image(image_bytes):
    img = Image.open(io.BytesIO(image_bytes)).convert("RGB")
    img = img.resize(IMG_SIZE)
    img_array = np.array(img) / 255.0
    img_array = np.expand_dims(img_array, axis=0)
    return img_array

def register_predict_route(app):
    @app.route("/predict", methods=["POST"])
    def predict():
        logger.info("Received prediction request")
        
        if 'image' not in request.files:
            logger.warning("No image file provided in request")
            return jsonify({'error': 'No image file provided'}), 400
        
        try:
            # Get form data
            file = request.files['image']
            sex = request.form.get('sex', 'male').lower() 
            age = request.form.get('age', '40')
            anatom_site = request.form.get('anatom_site', 'torso').lower()
            
            logger.info(f"Processing request: sex={sex}, age={age}, site={anatom_site}")
            
            # Validate inputs
            if sex not in ['male', 'female']:
                logger.warning(f"Invalid sex value: {sex}")
                return jsonify({'error': "Sex must be 'male' or 'female'"}), 400
                
            if anatom_site not in ANATOM_SITE_CATEGORIES:
                logger.warning(f"Invalid anatomical site: {anatom_site}")
                return jsonify({'error': f"Invalid anatomical site. Must be one of: {ANATOM_SITE_CATEGORIES}"}), 400
                
            try:
                age = float(age)
                if not (0 < age <= 120):
                    raise ValueError
            except ValueError:
                logger.warning(f"Invalid age value: {age}")
                return jsonify({'error': 'Age must be a number between 1 and 120'}), 400
            
            # Process image in memory
            logger.info("Reading and preprocessing image")
            img_bytes = file.read()
            img_array = preprocess_image(img_bytes)
            
            # Preprocess metadata
            sex_code = 0 if sex == 'male' else 1
            anatom_code = ANATOM_SITE_CATEGORIES.index(anatom_site)
            age_norm = (age - AGE_MIN) / (AGE_MAX - AGE_MIN)
            
            # Make prediction
            logger.info("Running model inference")
            pred = model.predict([
                img_array,
                np.array([[sex_code]]),
                np.array([[anatom_code]]),
                np.array([[age_norm]])
            ])
            
            # Prepare response
            mel_prob = float(pred[0][0])
            nv_prob = float(pred[0][1])
            interpretation = interpret_prediction(pred[0])
            
            logger.info(f"Prediction results: Melanoma={mel_prob:.4f}, Nevus={nv_prob:.4f}")
            logger.info(f"Interpretation: {interpretation}")
            
            return jsonify({
                'diagnosis': {
                    'Melanoma': mel_prob,
                    'Nevus': nv_prob
                },
                'interpretation': interpretation
            })
            
        except Exception as e:
            logger.error(f"Error processing request: {str(e)}", exc_info=True)
            return jsonify({'error': str(e)}), 500

    logger.info("Prediction route registered")
    return app