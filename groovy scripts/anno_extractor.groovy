import qupath.lib.images.servers.LabeledImageServer

def imageData = getCurrentImageData()

// Define output path (relative to project)
def name = GeneralTools.getNameWithoutExtension(imageData.getServer().getMetadata().getName())
def pathOutput = buildFilePath(PROJECT_BASE_DIR, 'Tiles_0.9_256_64ov_anno_only', name)
mkdirs(pathOutput)

// Define output resolution
double requestedPixelSize = 0.9

// Convert to downsample
double downsample = requestedPixelSize / imageData.getServer().getPixelCalibration().getAveragedPixelSize()

// Create an ImageServer where the pixels are derived from annotations
def labelServer = new LabeledImageServer.Builder(imageData)
    .backgroundLabel(0, ColorTools.WHITE) // Specify background label (usually 0 or 255)
    .downsample(downsample)    // Choose server resolution; this should match the resolution at which tiles are exported
    .addLabel('Tumor', 1)      // Choose output labels (the order matters!)
    .addLabel('Stroma', 2)
    .addLabel('Necrosis', 3)
    .addLabel('Verhornung', 4)
    .addLabel('Blut', 5)
    .addLabel('Lockeres Bindegewebe', 6)
    .addLabel('Zellreiches Stroma', 7)
    .addLabel('Fettgewebe', 8)
    .addLabel('Muskel', 9)
    .addLabel('cauter', 10)
    .addLabel('Muskel längs', 11)
    .addLabel('Nerv', 12)
    
    .multichannelOutput(false)  // If true, each label is a different channel (required for multiclass probability)
    .build()

// Create an exporter that requests corresponding tiles from the original & labeled image servers
new TileExporter(imageData)
    .downsample(downsample)     // Define export resolution
    .imageExtension('.png')     // Define file extension for original pixels (often .tif, .jpg, '.png' or '.ome.tif')
    .tileSize(256)              // Define size of each tile, in pixels
    .labeledServer(labelServer) // Define the labeled image server to use (i.e. the one we just built)
    .annotatedTilesOnly(true)   // If true, only export tiles if there is a (labeled) annotation present
    .overlap(64)                // Define overlap, in pixel units at the export resolution
    .writeTiles(pathOutput)     // Write tiles to the specified directory

print 'Done!'