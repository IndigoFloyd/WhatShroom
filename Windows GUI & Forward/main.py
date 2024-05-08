from ui import *
import sys
from PyQt5 import QtCore, QtWidgets, QtGui

if __name__ == "__main__":
    app = QtWidgets.QApplication(sys.argv)
    controller_ = controller()
    controller_.show_main()
    sys.exit(app.exec_())