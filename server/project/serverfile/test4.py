from module import callocrApi,recognize_box
import json

temp = []
temp.append("일기")
temp.append("잘 익은 똥을 누고 난 다음")
temp.append("너, 가련한 육체여")
temp.append("살 것 같으니 술 생각 나냐?")

str = {}
str["result"]  = {}
str["result"]["recognition_words"] = temp

print("[recognize] output:\n{}\n".format(json.dumps(str, sort_keys=True, indent=2, ensure_ascii = False)))

