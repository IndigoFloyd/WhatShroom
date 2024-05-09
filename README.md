🍄A toxic mushroom detection project based on YOLOv5, deployed on a cloud server and running on Android devices.🍄

⭐**Star us if you like our work~**⭐
## 1  Why to do 😕
### 1.1  Background
Mushrooms, revered for their unique flavor and rich nutritional value, have long been a staple in human cuisine. However, this culinary delight harbors a sinister side, as the vast array of mushroom species, with their diverse morphological characteristics, often blurs the line between edible and toxic varieties, posing a significant safety hazard to wild mushroom consumption.

Statistics paint a grim picture, revealing China as a hotspot for wild mushroom poisoning incidents, with Yunnan Province, a "mushroom-consuming powerhouse," bearing the brunt of this public health crisis. Data from 2020 indicates that the number of fatalities caused by wild mushroom poisoning in Yunnan Province far exceeds those attributed to the COVID-19 pandemic. This alarming trend underscores the urgent need to address wild mushroom poisoning as a critical food safety issue.

Traditionally, mushroom toxicity has been determined primarily through empirical judgment, a method fraught with limitations. Not only does this approach risk misclassifying edible mushrooms as poisonous, but it also leads to the wasteful discarding of potentially edible species. In recent years, physicochemical analysis techniques have been employed for mushroom toxicity detection; however, these methods are often hindered by high costs, complex procedures, and narrow detection ranges, rendering them impractical for routine wild mushroom safety assessment.

The rapid advancement of deep learning technology has propelled image recognition-based mushroom classification and identification methods to the forefront of research. These methods leverage deep learning models to extract and classify features from mushroom images, enabling rapid and accurate mushroom identification. Despite their promise, existing mushroom identification methods primarily rely on static images, limiting their recognition accuracy and robustness. Additionally, these methods often lack portability and practicality.

To address these shortcomings, this project proposes a novel mobile mushroom species/toxicity detection software development scheme that integrates deep learning and cloud computing. This endeavor aims to provide the public with scientifically sound and effective guidance for safe wild mushroom consumption, thereby reducing the incidence of wild mushroom poisoning incidents.
### 1.2  TODO List
- Label toxic mushroom images and construct a dataset; 😰
- Train an efficient and precise model; ✅
- Deploy the deep learning model on a cloud server; ✅
- Develop an Android app; ✅
## 2  How to do 🤔
### 2.1  Construct the dataset
There are many kinds of poisonous mushroom. Due to personal limitations, only common Chinese poisonous mushrooms will be considered in the dataset. The list of species is referenced from this literature: 包海鹰, 李玉. 中国毒蘑菇名录\[J]. 菌物学报, 2014, 33(3): 517-548. In this project, this list locates at ```WhatShroom/Windows GUI & Forward/classes.txt```
Image resources are from this dataset: Wilson, N., Hollinger, J., et al. 2006-present. Mushroom Observer. [https://mushroomobserver.org](https://mushroomobserver.org/). I wrote a python script for filtering the poisonous mushroom species that are in the above list and have more than 150 images, and downloading the corresponding images from the dataset.
In this way, a dataset is constructed encompassing 64 toxic mushroom species, with 150 images per species included. However, only the first 5 species are labeled.
### 2.2  Train the model
The dataset was split into training, validation, and test sets following a 0.8/0.1/0.1 ratio. Finally, there are 540 images as the training set, 68 images as the test set and 67 images as the validation set. 
I chose YOLO v5s as my baseline, for it's high efficiency and amazing precision. With the improvement of my knowledge of deep learning, lately I inserted the CBAM block into the head. The comparison results are as follows.
<div align=center>
<img src="Windows GUI & Forward/YOLO v5 with CBAM/comparison.svg"/>
</div>
<div align=center>
<center>Fig.1 Comparison Results of YOLOv5s and YOLOv5s+CBAM</center>
</div>
It is clear that the addition of the CBAM module improves the generalization performance and stability of the model, surpassing the baseline model in the vast majority of metrics.

### 2.3  Deploy the model on a cloud server
To improve the performance of forwarding, an efficient way is exporting the model as ONNX format. We could do this easily by running script ```export.py```
provided by [Ultralytics](https://github.com/ultralytics/yolov5).
Outputs' shape is $[1, 25200, 10]$, with each representing **batchsize**, **number of anchors**, **(center point x-coordinate, center point y-coordinate, width of anchor frames, height of anchor frames, confidence, and category scoring corresponding to each of the 5 categories, respectively)**. Here is a simple example code to resolve outputs.
```python
for out in outs:
    if out[4] > 0.8:
		# upper left
		x1 = int((out[0] - 0.5 * out[2]) * scaleX)
		y1 = int((out[1] - 0.5 * out[3]) * scaleY)
		# lower right
		x2 = int((out[0] + 0.5 * out[2]) * scaleX)
		y2 = int((out[1] + 0.5 * out[3]) * scaleY)
		# scores of each class
		classes_scores = out[5:]
		# class index of the highest score
		_, _, _, max_idx = cv2.minMaxLoc(classes_scores)  
```
As for the framework of the backend, Flask is a good choice for it's easy to get started by Python developers. An Jupyter Notebook example is provided at ```
WhatShroom/Cloud Server/cloud.ipynb```. It claims a class ```Detect``` to resolve outputs, and has a function ```upload_file``` to process the POST requests from other client like WhatShroom Android APP. By default this backend program will run on port ```5000```.
### 2.4  Develop an APP with GUI
For easier access, an APP with GUI is significant. Here we developed for 2 different platforms: Windows and Android.
#### 2.4.1  Windows version 💻
We use Qt Designer and PyQt to develop the UI of the Windows version app. To manage the window jumps, we claimed a class ```controller```, which receives different signals and jumps to the correct window. The workflow is completely running locally, cause the common computers are already powerful enough for model inference.
Here is a simple demonstration:
<div align=center>
<img src="Windows GUI & Forward/WhatShroom_win.gif"/>
</div>

#### 2.4.2  Android version 📱
We also developed the Android version, using Android Studio. It runs smoothly on the Huawei Mate 40E phone for testing. Considering the performance of the mobile platform, the inference step is running on your self-built server(refer to [Deploy the model on a cloud server](#2.3-Deploy-the-model-on-a-cloud-server).
The construction looks like as follows:
<div align=center>
<img src="Android APP/construction.png"/>
</div>
<div align=center>
<center>Fig.2 Construction of the Android APP</center>
</div>
And here is a simple demonstration:
<div align=center>
<img src="Android APP/WhatShroom.gif"/>
</div>


