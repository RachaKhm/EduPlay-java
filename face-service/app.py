from flask import Flask, request, jsonify
import numpy as np
import cv2
import base64
from deepface import DeepFace
import os

app = Flask(__name__)

# Modèle à utiliser (VGG-Face est léger et précis)
MODEL_NAME = "VGG-Face"

def decode_base64_image(base64_str):
    if "," in base64_str:
        base64_str = base64_str.split(",")[1]
    img_data = base64.b64decode(base64_str)
    nparr = np.frombuffer(img_data, np.uint8)
    return cv2.imdecode(nparr, cv2.IMREAD_COLOR)

@app.route('/embed', methods=['POST'])
def embed():
    try:
        data = request.json
        img_b64 = data.get("image")
        if not img_b64:
            return jsonify({"status": "error", "message": "No image provided"}), 400

        img = decode_base64_image(img_b64)
        
        # Extraire l'embedding
        # enforce_detection=True permet de s'assurer qu'un visage est présent
        results = DeepFace.represent(img, model_name=MODEL_NAME, enforce_detection=True)
        
        if not results:
            return jsonify({"status": "error", "message": "No face detected"}), 400

        embedding = results[0]["embedding"]
        return jsonify({"status": "ok", "embedding": embedding})

    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/compare', methods=['POST'])
def compare():
    try:
        data = request.json
        emb1 = data.get("embedding1")
        emb2 = data.get("embedding2")
        threshold = data.get("threshold", 0.85)

        if not emb1 or not emb2:
            return jsonify({"status": "error", "message": "Missing embeddings"}), 400

        # Calculer la distance cosinus (plus petite = plus proche)
        # DeepFace utilise souvent la distance cosinus
        a = np.array(emb1)
        b = np.array(emb2)
        
        distance = 1 - (np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b)))
        
        # Inverser pour correspondre à la logique de l'app (match si distance < (1-threshold))
        # Ou plus simple: DeepFace considère un match si distance < 0.40 pour VGG-Face
        match = distance < 0.40

        return jsonify({"status": "ok", "match": bool(match), "distance": float(distance)})

    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

if __name__ == '__main__':
    print(f"🚀 Serveur Facial EduPlay démarré sur http://127.0.0.1:5001")
    print(f"Using model: {MODEL_NAME}")
    app.run(host='127.0.0.1', port=5001)
