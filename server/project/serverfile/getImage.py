from flask_restful import Resource, reqparse
from flask import send_file
from module import recognize_box, db
from werkzeug.datastructures import FileStorage
import os

class GetImage(Resource):
    def get(self):
        try:
            parser = reqparse.RequestParser()
            parser.add_argument('filename',type=str)
            args = parser.parse_args()
            filename = args['filename']
            file_path = './upload/'+filename+'_resized.jpg'
            if os.path.isfile(os.path.join(file_path)) == True:
                return send_file(file_path)
            else:
                return {'result':'fail', 'info':'file no exist'}
        except Exception as e:
            return {'error': str(e)}

    def post(self):
        try:
            parser = reqparse.RequestParser()
            parser.add_argument('image',type=FileStorage,location='files')
            args= parser.parse_args()

            images = args['image']

            images.save('./upload/{0}'.format(images.filename))
            image_path = './upload/'+images.filename
            if images.filename[-11:] == "resized.jpg":
                filename = images.filename
                url = 'http://bustercallapi.r-e.kr/getImage?filename='+filename[:-12]
            else:
                filename = images.filename
                filename = filename[:-4]
                url = 'http://bustercallapi.r-e.kr/getImage?filename='+filename
            recog = recognize_box.RecogArea(image_path)
            recog.drawBoxImage()
            box = recog.box
            box = str(box)
            
            return {'RESPONSE':'200 OK','result':'success','box':box ,'url':url, 'filename':recog.image_path[9:]}
        
        except Exception as e:
            return {'error':str(e)}
