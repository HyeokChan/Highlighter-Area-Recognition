import cv2, json
import numpy as np
from module import callocrApi

image_path = './ocrtest/2_h.JPG'
img_color = cv2.imread(image_path)
height,width = img_color.shape[:2]
ratio = float(1024) / max(height,width)
image_path = "{}_resized.jpg".format(image_path)
cv2.imwrite(image_path,img_color)
img_color = cv2.resize(img_color, None, fx=ratio, fy=ratio)
img_hsv = cv2.cvtColor(img_color, cv2.COLOR_RGB2HSV)

box = []
lower_green = (50, 100, 100)
upper_green = (100, 255, 255)
lower_blue = (0,50,100)
upper_blue = (30,255,255)

img_maskB = cv2.inRange(img_hsv, lower_blue, upper_blue)
img_maskG = cv2.inRange(img_hsv, lower_green, upper_green)

kernel = np.ones((3,3),np.uint8)
img_maskB = cv2.morphologyEx(img_maskB,cv2.MORPH_OPEN, kernel)
img_maskB = cv2.morphologyEx(img_maskB,cv2.MORPH_CLOSE, kernel)

kernel = np.ones((3,3),np.uint8)
img_maskG = cv2.morphologyEx(img_maskG,cv2.MORPH_OPEN, kernel)
img_maskG = cv2.morphologyEx(img_maskG,cv2.MORPH_CLOSE, kernel)

img_mask = cv2.bitwise_or(img_maskB,img_maskG)
img_result = cv2.bitwise_and(img_color, img_color, mask = img_mask)


_, _, statsB, centroidsB = cv2.connectedComponentsWithStats(img_maskB)

for idx, centroid in enumerate(centroidsB):
    if statsB[idx][0] == 0 and statsB[idx][1] == 0:
        continue

    if np.any(np.isnan(centroid)):
        continue

    x,y,width,height,area = statsB[idx]
    centerX,centerY = int(centroid[0]), int(centroid[1])
    if area>200:
        addpoint = 2
        temp = []
        leftU = [x-addpoint,y-addpoint]
        rightB = [x+width+addpoint,y+height+addpoint]
        temp.append(leftU)
        temp.append(rightB)
        box.append(temp)
        cv2.rectangle(img_color, (x-2,y-2), (x+width+2,y+height+2), (0,0,255))

_, _, statsG, centroidsG = cv2.connectedComponentsWithStats(img_maskG)

for idx, centroid in enumerate(centroidsG):
    if statsG[idx][0] == 0 and statsG[idx][1] == 0:
        continue

    if np.any(np.isnan(centroid)):
        continue

    x,y,width,height,area = statsG[idx]
    centerX,centerY = int(centroid[0]), int(centroid[1])
    if area>200:
        addpoint = 2
        temp = []
        leftU = [x-addpoint,y-addpoint]
        rightB = [x+width+addpoint,y+height+addpoint]
        temp.append(leftU)
        temp.append(rightB)
        box.append(temp)
        cv2.rectangle(img_color, (x,y), (x+width+2,y+height-1), (0,0,255))

#img_color = img_color[497:521,109:226]
cv2.imshow('img_color', img_color)
cv2.imwrite('./upload/5_box.jpg',img_color)
#cv2.imshow('img_mask', img_mask)
#cv2.imshow('img_result', img_result)
#print(box)
max_width = 0
total_height = 0
images = []
print(box)
for a in box :
    temp_img = img_color[a[0][1]:a[1][1],a[0][0]:a[1][0]]
    images.append(temp_img)
    if images[-1].shape[1] > max_width:
        max_width = images[-1].shape[1]
    total_height += images[-1].shape[0]
print(total_height,max_width)
final_image = np.zeros((total_height+50,max_width+50,3),dtype=np.uint8)
current_y = 0
for image in images:
    # add an image to the final array and increment the y coordinate
    final_image[current_y:image.shape[0]+current_y,:image.shape[1],:] = image
    current_y += image.shape[0]+3

cv2.imshow("abc",final_image)
    #cv2.waitKey(0)
    #cv2.destroyAllWindows()


cv2.waitKey(0)
cv2.destroyAllWindows()
image_path = "{}_added.jpg".format(image_path)
cv2.imwrite(image_path,final_image)
ocr = callocrApi.OCRAPI()
output = ocr.kakao_ocr_detect(image_path).json()
boxes = output["result"]["boxes"]
boxes = boxes[:min(len(boxes),ocr.limit_box)]
output = ocr.kakao_ocr_recognize(image_path,boxes).json()
print("[recognize] output:\n{}\n".format(json.dumps(output, sort_keys=True, indent=2, ensure_ascii = False)))