from flask import jsonify

def register_home_route(app):
    @app.route('/', methods=['GET'])
    def home():
        return jsonify({"MNI": "project"}), 200
