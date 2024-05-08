import cv2
import torch
from torchvision.ops.boxes import batched_nms


class Detect:
    def __init__(self, img, classPath='./classes.txt' ,modelPath='./weights/best.onnx'):
        self.classes = open(classPath).read().split("\n")  # 读取类别
        self.model = cv2.dnn.readNetFromONNX(modelPath)  # 读取模型
        self.img = cv2.imread(img)  # 读取图片

    def preProcessing(self):
        self.img = cv2.resize(self.img, (640, 640))  # 裁剪大小
        blob = cv2.dnn.blobFromImage(self.img, 1/255.0, swapRB=True)  # 由图片加载数据，这里还可以进行缩放、归一化等预处理
        return blob

    def run(self, blob):
        self.model.setInput(blob)  # 输入图像
        outs = self.model.forward(self.model.getUnconnectedOutLayersNames())[-1][0]  # 前向推理，-1表示最后一层，取后[1,25200,10]
        return outs

    def afterProcessing(self, outs):
        boxes_list = []  # 监测框
        classes_list = []  # 类别列表
        score_list = []  # 打分
        for out in outs:
            if out[4] > 0.8:
                x1 = int((out[0] - 0.5 * out[2]))  # 左上
                y1 = int((out[1] - 0.5 * out[3]))  # 左上
                x2 = int((out[0] + 0.5 * out[2]))  # 右下
                y2 = int((out[1] + 0.5 * out[3]))  # 右下
                classes_scores = out[5:]  # 各种类别的分数
                _, _, _, max_idx = cv2.minMaxLoc(classes_scores)  # 最高分数的类别
                class_id = max_idx[1]  # 取类别id
                boxes_list.append([x1, y1, x2, y2])  # 加入矩形框列表
                classes_list.append(class_id)  # 加入类别列表
                score_list.append(classes_scores[class_id])  # 加入分数列表
        boxes_list_tensor = torch.Tensor(boxes_list)  # 将矩形框转为张量
        classes_list_tensor = torch.Tensor(classes_list)  # 将类别转为张量
        score_list_tensor = torch.Tensor(score_list)  # 将分数转为张量
        nms_list = batched_nms(boxes_list_tensor, score_list_tensor, classes_list_tensor, 0.001)  # nms
        final_box = []
        final_class = []
        final_score = []
        for index in nms_list:
            index = int(index)  # tensor -> int
            final_box.append(boxes_list[index])  # 获取矩形框坐标
            final_class.append(self.classes[classes_list[index]])  # 获取类别
            final_score.append(f'{score_list[index]:.2%}')  # 获取置信度
        return final_box, final_class, final_score

if __name__ == '__main__':
    detect = Detect(r"D:\test.jpg")
    blob = detect.preProcessing()  # 预处理图像
    outs = detect.run(blob)  # 前向推理
    final_box, final_class, final_score = detect.afterProcessing(outs)  # 后处理
    print(final_class)