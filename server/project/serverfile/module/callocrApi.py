import json

import cv2
import requests
import sys

class OCRAPI():
    def __init__(self):
        self.appkey = '6d725fe46f9da303130b951d2314c6bd'
        self.limit_px = 1024
        self.limit_byte = 1024*1024
        self.limit_box = 40

    def kakao_ocr_resize(self,image_path: str):

        image = cv2.imread(image_path)
        height, width, _ = image.shape

        if self.limit_px < height or self.limit_px < width:
            ratio = float(self.limit_px) / max(height, width)
            image = cv2.resize(image, None, fx=ratio, fy=ratio)
            height, width, _ = height, width, _ = image.shape

            # api 사용전에 이미지가 resize된 경우, recognize시 resize된 결과를 사용해야함.
            image_path = "{}_resized.jpg".format(image_path)
            cv2.imwrite(image_path, image)

            return image_path
        return None


    def kakao_ocr_detect(self,image_path: str):
        API_URL = 'https://kapi.kakao.com/v1/vision/text/detect'

        headers = {'Authorization': 'KakaoAK {}'.format(self.appkey)}

        image = cv2.imread(image_path)
        jpeg_image = cv2.imencode(".jpg", image)[1]
        data = jpeg_image.tobytes()

        return requests.post(API_URL, headers=headers, files={"file": data})


    def kakao_ocr_recognize(self,image_path: str, boxes: list):
        
        result = []
        for box in boxes:
            temp=[]
            if not result:
                temp.append(box[0])
                temp.append(box[1])
                temp.append(box[2])
                temp.append(box[3])
                result.append(temp)
            else: 
                if abs(box[1][1] - result[-1][1][1]) <=3:
                    if abs(box[2][1] - result[-1][2][1]) <=3:
                        result[-1][1] = box[1]
                        result[-1][2] = box[2]
                else:
                    temp.append(box[0])
                    temp.append(box[1])
                    temp.append(box[2])
                    temp.append(box[3])
                    result.append(temp)

        API_URL = 'https://kapi.kakao.com/v1/vision/text/recognize'

        headers = {'Authorization': 'KakaoAK {}'.format(self.appkey)}

        image = cv2.imread(image_path)
        jpeg_image = cv2.imencode(".jpg", image)[1]
        data = jpeg_image.tobytes()

        return requests.post(API_URL, headers=headers, files={"file": data}, data={"boxes": json.dumps(result)})


def main():
    if len(sys.argv) != 1:
        print("Please run with args: $ python example.py /path/to/image appkey")
    image_path = sys.argv[1]
    appkey = '6d725fe46f9da303130b951d2314c6bd'
    resize_impath = kakao_ocr_resize(image_path)
    if resize_impath is not None:
        image_path = resize_impath
        print("원본 대신 리사이즈된 이미지를 사용합니다.")

    output = kakao_ocr_detect(image_path, appkey).json()
    print("[detect] output:\n{}\n".format(output))

    boxes = output["result"]["boxes"]
    boxes = boxes[:min(len(boxes), LIMIT_BOX)]
    output = kakao_ocr_recognize(image_path, boxes, appkey).json()
    print("[recognize] output:\n{}\n".format(json.dumps(output, sort_keys=True, indent=2, ensure_ascii = False)))