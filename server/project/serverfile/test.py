import requests,json,cv2
from module import callocrApi

# url = 'http://bustercallapi.r-e.kr/ocr'
# files = {'image': open('test.png','rb')}
# requests.post(url,files=files)

ocrApi = callocrApi.OCRAPI()

image_path = './test.png'
print(ocrApi.appkey)

#output = ocrApi.kakao_ocr_detect(image_path).json()
#print("[detect] output:\n{}\n".format(output))

#boxes = output["result"]["boxes"]
#boxes = boxes[:min(len(boxes), ocrApi.limit_box)]
boxes1 = [[[280, 8], [451, 5], [451, 20], [281, 22]], [[4, 38], [50, 38], [50, 53], [4, 53]], [[64, 38], [108, 38], [108, 54], [64, 54]], [[123, 38], [169, 38], [169, 53], [123, 53]], [[182, 38], [226, 38], [226, 53], [182, 53]], [[239, 38], [298, 38], [298, 53], [239, 53]], [[312, 38], [372, 38], [372, 53], [312, 53]], [[0, 64], [164, 64], [164, 75], [0, 75]], [[167, 63], [193, 63], [193, 75], [167, 75]], [[196, 61], [207, 61], [207, 77], [196, 77]]]
boxes2 = [[[280, 8], [451, 5], [451, 20], [281, 22]], [[4, 38], [372, 38], [372, 53], [4, 53]],[[0, 64], [164, 64], [164, 75], [0, 75]], [[167, 63], [193, 63], [193, 75], [167, 75]], [[196, 61], [207, 61], [207, 77], [196, 77]]]

print(boxes1)
print("-----------------------")
print(boxes2)
output = ocrApi.kakao_ocr_recognize(image_path, boxes1).json()
output = output['result']['recognition_words']
print("[recognize] output:\n{}\n".format(json.dumps(output, sort_keys=True, indent=2, ensure_ascii = False)))

output2 = ocrApi.kakao_ocr_recognize(image_path,boxes2).json()
output2 = output2['result']['recognition_words']
print("[recognize] output2:\n{}\n".format(json.dumps(output2, sort_keys=True, indent=2, ensure_ascii = False)))
