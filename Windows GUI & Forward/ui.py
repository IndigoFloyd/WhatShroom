import torch
from PyQt5 import QtCore, QtWidgets, QtGui
from MainWindow import Ui_Form
from result import Ui_Form1
from canvas import Ui_Show
import cv2
import numpy as np
from torchvision.ops.boxes import batched_nms


class MainWindow(QtWidgets.QMainWindow, Ui_Form):
    signal = QtCore.pyqtSignal()
    signal_path = QtCore.pyqtSignal(str)  # 传递信号用
    img_path = r""

    def __init__(self, parent=None):
        super(MainWindow, self).__init__(parent)
        self.setupUi(self)
        self.OpenImage.clicked.connect(self.select)
        self.pushButton.hide()
        self.pushButton.setEnabled(False)
        self.pushButton.clicked.connect(self.switch)

    def select(self):
        self.img_path = QtWidgets.QFileDialog.getOpenFileName(self, "选择图片", "", "*.jpg;;*.png;;All Files(*)")[0]
        self.Path.setText(self.img_path)
        self.pushButton.show()
        self.pushButton.setEnabled(True)

    def switch(self):
        self.signal.emit()
        self.signal_path.emit(self.img_path)


class ResultWindow(QtWidgets.QMainWindow, Ui_Form1):
    signal_home = QtCore.pyqtSignal()
    path = ""

    def __init__(self, parent=None):
        super(ResultWindow, self).__init__(parent)
        self.setupUi(self)

    def closeEvent(self, a0: QtGui.QCloseEvent) -> None:
        self.signal_home.emit()

    def detect(self):
        classes = open("./classes.txt").read().split("\n")  # 所有类别
        net = cv2.dnn.readNetFromONNX("./weights/best.onnx")  # 加载训练好的识别模型
        image = cv2.imread(self.path)  # 读取图片
        image = cv2.resize(image, (640, 640))
        blob = cv2.dnn.blobFromImage(image, 1/255.0, swapRB=True)  # 由图片加载数据 这里还可以进行缩放、归一化等预处理
        net.setInput(blob)  # 设置模型输入
        outs = net.forward(net.getUnconnectedOutLayersNames())[-1][0]
        class_name = ""
        boxes_list = []
        classes_list = []
        score_list = []
        for out in outs:
            if out[4] > 0.8:
                x1 = int((out[0] - 0.5 * out[2]))
                y1 = int((out[1] - 0.5 * out[3]))
                x2 = int((out[0] + 0.5 * out[2]))
                y2 = int((out[1] + 0.5 * out[3]))
                classes_scores = out[5:]
                _, _, _, max_idx = cv2.minMaxLoc(classes_scores)
                class_id = max_idx[1]
                boxes_list.append([x1, y1, x2, y2])
                classes_list.append(class_id)
                score_list.append(classes_scores[class_id])
        boxes_list = torch.Tensor(boxes_list)
        classes_list = torch.Tensor(classes_list)
        score_list = torch.Tensor(score_list)
        nms_list = batched_nms(boxes_list, score_list, classes_list, 0.001)
        final_class = []
        for index in nms_list:
            index = index.item()
            coordinate = list(map(int, boxes_list[index].tolist()))
            class_name = classes[int(classes_list[index].item())]
            final_class.append(class_name)
            print(coordinate)
            cv2.rectangle(image, coordinate, (255, 0, 0), 2)
            cv2.putText(image, class_name, ((coordinate[0] - 20), (coordinate[1] - 10)), cv2.FONT_HERSHEY_SIMPLEX, 0.5,
                                            (255, 0, 0), thickness=1, lineType=cv2.LINE_AA)
        image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
        show_image = QtGui.QImage(image.data, image.shape[1], image.shape[0], QtGui.QImage.Format_RGB888)
        self.image_shower.setPixmap(QtGui.QPixmap.fromImage(show_image))
        self.image_shower.setScaledContents(True)
        final_class = set(final_class)
        output = ""
        for c in final_class:
            output += c + '\n'
        self.mushroomInfo.setText(output)

class CanvasWindow(QtWidgets.QMainWindow, Ui_Show):
    signal_confirm = QtCore.pyqtSignal()
    signal_cancel = QtCore.pyqtSignal()
    signal_path = QtCore.pyqtSignal(str)
    path = ""

    def __init__(self):
        super(CanvasWindow, self).__init__()
        self.setupUi(self)
        self.cancel.clicked.connect(self.Cancel)
        self.confirm.clicked.connect(self.Confirm)

    def Cancel(self):
        self.signal_cancel.emit()

    def Confirm(self):
        self.signal_confirm.emit()
        self.signal_path.emit(self.path)

    def showPix(self):
        pix = QtGui.QPixmap(self.path)
        self.canvas.setPixmap(pix)
        self.canvas.setScaledContents(True)  # 图片自适应label大小


class controller:
    def __init__(self):
        self.window = MainWindow()
        self.canvas = CanvasWindow()
        self.resultwindow = ResultWindow()

    def show_main(self):
        self.window.signal.connect(self.show_canvas)
        self.window.signal_path.connect(self.sendPath)  # 用sendPath函数接收地址参数
        self.canvas.close()
        self.resultwindow.close()
        self.window.show()

    def sendPath(self, path):
        self.canvas.path = path
        self.canvas.showPix()

    def show_canvas(self):
        self.canvas.signal_cancel.connect(self.show_main)
        self.canvas.signal_confirm.connect(self.show_resultwindow)
        self.canvas.signal_path.connect(self.sendPath_)
        self.window.close()
        self.canvas.show()

    def sendPath_(self, path):
        self.resultwindow.path = path
        self.resultwindow.detect()

    def show_resultwindow(self):
        self.canvas.close()
        self.resultwindow.show()
        self.resultwindow.signal_home.connect(self.show_main)