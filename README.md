🍄A toxic mushroom detection project based on YOLOv5, deployed on a cloud server and running on Android devices.🍄
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
There are many kinds of poisonous mushroom. Due to personal limitations, only common Chinese poisonous mushrooms will be considered in the dataset. The list of species is referenced from this literature: 包海鹰, 李玉. 中国毒蘑菇名录\[J]. 菌物学报, 2014, 33(3): 517-548. In this project, this list locates at```
```
WhatShroom/Windows GUI & Forward/classes.txt
```
Image resources are from this dataset: Wilson, N., Hollinger, J., et al. 2006-present. Mushroom Observer. [https://mushroomobserver.org](https://mushroomobserver.org/). I wrote a python script for filtering the poisonous mushroom species that are in the above list and have more than 150 images, and downloading the corresponding images from the dataset.
In this way, a dataset is constructed encompassing 64 toxic mushroom species, with 150 images per species included. However, only the first 5 species are labeled.
### 2.2  Train the model
The dataset was split into training, validation, and test sets following a 0.8/0.1/0.1 ratio. Finally, there are 540 images as the training set, 68 images as the test set and 67 images as the validation set. 
I chose YOLO v5s as my baseline, for it's high efficiency and amazing precision. With the improvement of my knowledge of deep learning, lately I inserted the CBAM block into the head. The comparison results are as follows(True Positive rate).

|        species        | YOLO v5s | YOLO v5s + CBAM |
|:---------------------:|:--------:|:---------------:|
| Agaricus xanthodermus |   0.90   |      0.95       |
|   Aleuria aurantia    |   0.62   |      0.72       |
|  Amanita bisporigera  |   1.00   |      0.96       |
|   Amanita farinosa    |   0.84   |      0.88       |
|   Amanita muscaria    |   0.73   |      0.82       |
