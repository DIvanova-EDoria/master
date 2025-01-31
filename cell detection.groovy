setImageType('FLUORESCENCE');
createFullImageAnnotation(true)
runPlugin('qupath.imagej.detect.cells.WatershedCellDetection', '{"detectionImage":"Channel 3","requestedPixelSizeMicrons":0.5,"backgroundRadiusMicrons":10.0,"backgroundByReconstruction":true,"medianRadiusMicrons":0.0,"sigmaMicrons":1.5,"minAreaMicrons":20.0,"maxAreaMicrons":800.0,"threshold":100.0,"watershedPostProcess":false,"cellExpansionMicrons":5.0,"includeNuclei":true,"smoothBoundaries":true,"makeMeasurements":true}')
