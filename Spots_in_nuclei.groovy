/* Find Spots in Nuclei
 * 
 * QuPath 0.5.0
 * v1.0 31-Jan-2024 NL
 */    



import qupath.lib.images.servers.ImageServerMetadata
import qupath.lib.roi.RectangleROI
import qupath.lib.objects.PathAnnotationObject
setImageType('FLUORESCENCE');


//removeObjects(getDetectionObjects().findAll{it->it.getPathClass() == getPathClass('Spot')},false)


// Define parameters
double downsample = 1
int channel = 0// zeros based!!!! so this is the 1st channel
//double threshold = 220
def above = getPathClass('Spot tmp')
def below = getPathClass('Ignore*')
double minArea = 0.5
double minHoleArea = 0







// Figure out the resolution from the current image
def server = getCurrentServer()
def cal = server.getPixelCalibration()
def resolution = cal.createScaledInstance(downsample, downsample)
def Metadata = server.getMetadata()
xres = Metadata.getPixelWidthMicrons();
yres = Metadata.getPixelHeightMicrons();


clearAllObjects()
createAllFullImageAnnotations(true)




/*
FOV = new RectangleROI(3.5/xres, 3.5/yres, 103.5/xres, 103.5/yres)
//FOV = new RectangleROI(1,1,256,128)
FOV = new PathAnnotationObject(FOV)
addObject(FOV)
selectObjects(FOV)
*/



runPlugin('qupath.imagej.detect.cells.WatershedCellDetection', '{"detectionImage":"Channel 2","requestedPixelSizeMicrons":0.5,"backgroundRadiusMicrons":10.0,"backgroundByReconstruction":true,"medianRadiusMicrons":0.0,"sigmaMicrons":2.0,"minAreaMicrons":10.0,"maxAreaMicrons":400.0,"threshold":300.0,"watershedPostProcess":true,"cellExpansionMicrons":0.0,"includeNuclei":true,"smoothBoundaries":true,"makeMeasurements":true}')


ROI = getAnnotationObjects()
removeObjects(ROI,true)
resetSelection()



CELLS_detec = getDetectionObjects()
def CELLS = CELLS_detec.collect {
    return PathObjects.createAnnotationObject(it.getROI(), it.getPathClass())
}
removeObjects(CELLS_detec, true)
addObjects(CELLS)


selectAnnotations();
runPlugin('qupath.lib.algorithms.IntensityFeaturesPlugin', '{"pixelSizeMicrons":'+0.2+',"region":"ROI","tileSizeMicrons":25.0,"channel1":true,"channel2":false,"doMean":true,"doStdDev":true,"doMinMax":false,"doHaralick":false,"haralickMin":NaN,"haralickMax":NaN,"haralickDistance":1,"haralickBins":32}')


FOV = new RectangleROI(3.5/xres, 3.5/yres, 103.5/xres, 103.5/yres)
FOV = new PathAnnotationObject(FOV)
FOV_ROI = FOV.getROI()
//addObject(FOV)

CellsToBeRemoved = []
CELLS.each {cell->
    cell_mRed = cell.getMeasurementList().get("ROI: 0.20 µm per pixel: Channel 1: Mean")
    cell_sRed = cell.getMeasurementList().get("ROI: 0.20 µm per pixel: Channel 1: Std.dev.")
    
    threshold = cell_mRed+3*cell_sRed
    thresholder = qupath.opencv.ml.pixel.PixelClassifiers.createThresholdClassifier(resolution, channel, threshold, below, above)

    selectObjects(cell)
    createDetectionsFromPixelClassifier(thresholder, minArea, minHoleArea)
    Spot = getDetectionObjects().findAll{it->it.getPathClass() == getPathClass('Spot tmp')}
    //removeObject(nuc_Anno,true)
    
     Spot.each{it->
        it.setPathClass(getPathClass('Spot'))
        getCurrentHierarchy().addObjectBelowParent(cell,it,true)
    }
    if (Spot) {
       cell.setPathClass(getPathClass('Positive')) 
    } else {
       cell.setPathClass(getPathClass('Negative')) 
    }
      
    a =  FOV_ROI.getGeometry()
     if ( cell.getROI().getGeometry().overlaps(FOV_ROI.getGeometry())){
      CellsToBeRemoved <<cell
  }

}
removeObjects(CellsToBeRemoved,false)
//createAllFullImageAnnotations(true)
//FOV = getAnnotationObjects().findAll{it-> it.getPathClass() = null}

