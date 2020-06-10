from module import recognize_box
from module import callocrApi
import cv2,json

for a in range(1,33):
    image_path = './ocrtest/'+str(a)+'_h.JPG'

    recog = recognize_box.RecogArea(image_path)
    recog.drawBoxImage()
    recog.newImage(recog.box)

    ocr = callocrApi.OCRAPI()

    output = ocr.kakao_ocr_detect(image_path).json()
    boxes = output["result"]["boxes"]

    output = ocr.kakao_ocr_recognize(image_path,boxes).json()
    print("[recognize] output:\n{}\n".format(json.dumps(output, sort_keys=True, indent=2, ensure_ascii = False)))


    output2 = ocr.kakao_ocr_detect(recog.image_path).json()
    boxes2 = output2["result"]["boxes"]

    output2 = ocr.kakao_ocr_recognize(recog.image_path,boxes2).json()
    print("[recognize] output:\n{}\n".format(json.dumps(output2, sort_keys=True, indent=2, ensure_ascii = False)))
    cv2.imshow("recog_area",recog.img_color)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

