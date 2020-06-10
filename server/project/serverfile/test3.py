import cv2,json,base64,io
from module import recognize_box, callocrApi,db

image_path = './ocrtest/1.JPG'
recog = recognize_box.RecogArea(image_path)
recog.drawBoxImage()
recog.newImage()
with open(image_path,'rb')as f:
    image = f.read()
image_encode = base64.b64encode(image)
dbc = db.Database()
sql = 'insert into ocr_img_table(a_id,doc_name,doc_content,image) values(%s,%s,%s,%s)'
dbc.execute(sql,("1","1","1",image_encode))

sql = 'select * from ocr_img_table where a_id = %s'
row = dbc.executeAll(sql,"1")
print(row[0]['a_id'])
with open('./upload/5555.jpg','wb') as f:
    f.write(base64.b64decode(row[0]['image']))
# print(recog.image_path)

# ocr = callocrApi.OCRAPI()
# output = ocr.kakao_ocr_detect(recog.image_path).json()
# boxes = output["result"]["boxes"]

# print(result)
# output = ocr.kakao_ocr_recognize(recog.image_path,boxes).json()
# print("[recognize] output:\n{}\n".format(json.dumps(output, sort_keys=True, indent=2, ensure_ascii = False)))
