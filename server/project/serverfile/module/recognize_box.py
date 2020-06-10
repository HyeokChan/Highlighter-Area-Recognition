import cv2
import numpy as np

class RecogArea():
    def __init__(self,image_path):
        self.image_path = image_path
        if self.image_path[-11:] == "resized.jpg":
            self.img_color = cv2.imread(image_path)
        else:
            self.img_color = cv2.imread(self.image_path)
            # height,width = img_color.shape[:2]
            # ratio = float(1024) / max(height,width)
            # self.img_color = cv2.resize(img_color, None, fx=ratio, fy=ratio)
            self.image_path2 = "{}_resized.jpg".format(self.image_path[:-4])
            self.image_path = self.image_path2
            cv2.imwrite(self.image_path2,self.img_color)
        self.img_hsv = cv2.cvtColor(self.img_color, cv2.COLOR_RGB2HSV)
        self.origin_img = self.img_color.copy()
        self.lower_green = (50, 50, 70)
        self.upper_green = (100, 255, 255)
        self.lower_blue = (0,80,95)
        self.upper_blue = (30,250,250)
        self.box = []
        self.final_image=[]
        
    def drawBoxImage(self):
        img_maskB = cv2.inRange(self.img_hsv, self.lower_blue, self.upper_blue)
        img_maskG = cv2.inRange(self.img_hsv, self.lower_green, self.upper_green)

        origin_mask = cv2.bitwise_or(img_maskB,img_maskG)

        kernel = np.ones((1,1),np.uint8)
        img_maskB = cv2.morphologyEx(img_maskB,cv2.MORPH_OPEN, kernel)
        img_maskB = cv2.morphologyEx(img_maskB,cv2.MORPH_CLOSE, kernel)

        kernel = np.ones((1,1),np.uint8)
        img_maskG = cv2.morphologyEx(img_maskG,cv2.MORPH_OPEN, kernel)
        img_maskG = cv2.morphologyEx(img_maskG,cv2.MORPH_CLOSE, kernel)

        img_mask = cv2.bitwise_or(img_maskB,img_maskG)
        self.img_result = cv2.bitwise_and(self.img_color, self.img_color, mask = img_mask)

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
                self.box.append(temp)
                cv2.rectangle(self.img_color, (x,y), (x+width+3,y+height+2), (0,0,255))

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
                self.box.append(temp)
                cv2.rectangle(self.img_color, (x,y), (x+width+2,y+height-1), (0,0,255))
    
    def newImage(self,boxes):
        max_width = 0
        total_height = 0
        images = []
        for a in boxes :
            temp_img = self.origin_img[a[0][1]:a[1][1],a[0][0]:a[1][0]]
            images.append(temp_img)
            if images[-1].shape[1] > max_width:
                max_width = images[-1].shape[1]
            total_height += images[-1].shape[0]
        #print(total_height,max_width)
        self.final_image = np.zeros((total_height+100,max_width+100,3),dtype=np.uint8)
        current_y = 0
        for image in images:
            # add an image to the final array and increment the y coordinate
            self.final_image[current_y:image.shape[0]+current_y,:image.shape[1],:] = image
            current_y += image.shape[0]+3
        self.image_path = "{}_added.jpg".format(self.image_path[:-12])
        cv2.imwrite(self.image_path,self.final_image)