package populator;

import exporter.SCMapExporter;
import exporter.SaveExporter;
import exporter.ScenarioExporter;
import generator.*;
import importer.SCMapImporter;
import importer.SaveImporter;
import map.*;
import util.ArgumentParser;
import util.FileUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public strictfp class MapPopulator {

    public static boolean DEBUG = false;

    private Path inMapPath;
    private Path outFolderPath;
    private String mapFolder;
    private String mapName;
    private SCMap map;
    private Path propsPath;
    private boolean populateSpawns;
    private boolean populateMexes;
    private boolean populateHydros;
    private boolean populateProps;
    private boolean populateAI;
    private boolean populateDecals;
    private int spawnCount;
    private int mexCountPerPlayer;
    private int hydroCountPerPlayer;
    private boolean populateTextures;
    private boolean keepCurrentDecals;
    private boolean keepLayer0;
    private int moveLayer0To;
    private boolean keepLayer1;
    private boolean keepLayer2;
    private boolean keepLayer3;
    private boolean keepLayer4;
    private boolean keepLayer5;
    private boolean keepLayer6;
    private boolean keepLayer7;
    private boolean keepLayer8;

    private BinaryMask resourceMask;
    private BinaryMask waterResourceMask;
    private BinaryMask plateauResourceMask;

    private FloatMask oldLayer1;
    private FloatMask oldLayer2;
    private FloatMask oldLayer3;
    private FloatMask oldLayer4;
    private FloatMask oldLayer5;
    private FloatMask oldLayer6;
    private FloatMask oldLayer7;
    private FloatMask oldLayer8;

    private SymmetryHierarchy symmetryHierarchy;

    public static void main(String[] args) throws IOException {

        Locale.setDefault(Locale.US);
        if (DEBUG) {
            Path debugDir = Paths.get(".", "debug");
            FileUtils.deleteRecursiveIfExists(debugDir);
            Files.createDirectory(debugDir);
        }

        MapPopulator populator = new MapPopulator();

        populator.interpretArguments(args);

        System.out.println("Populating map " + populator.inMapPath);
        populator.importMap();
        populator.populate();
        populator.exportMap();
        System.out.println("Saving map to " + populator.outFolderPath.toAbsolutePath());
        System.out.println("Terrain Symmetry: " + populator.symmetryHierarchy.getTerrainSymmetry());
        System.out.println("Team Symmetry: " + populator.symmetryHierarchy.getTeamSymmetry());
        System.out.println("Spawn Symmetry: " + populator.symmetryHierarchy.getSpawnSymmetry());
        System.out.println("Done");
    }

    public void interpretArguments(String[] args) {
        interpretArguments(ArgumentParser.parse(args));
    }

    private void interpretArguments(Map<String, String> arguments) {
        if (arguments.containsKey("help")) {
            System.out.println("map-transformer usage:\n" +
                    "--help                 produce help message\n" +
                    "--in-folder-path arg   required, set the input folder for the map\n" +
                    "--out-folder-path arg  required, set the output folder for the transformed map\n" +
                    "--team-symmetry arg    required, set the symmetry for the teams(X, Z, XZ, ZX)\n" +
                    "--spawn-symmetry arg   required, set the symmetry for the spawns(POINT, X, Z, XZ, ZX)\n" +
                    "--spawns arg           optional, populate arg spawns\n" +
                    "--mexes arg            optional, populate arg mexes per player\n" +
                    "--hydros arg           optional, populate arg hydros per player\n" +
                    "--props arg            optional, populate props arg is the path to the props json\n" +
                    "--wrecks               optional, populate wrecks\n" +
                    "--textures arg         optional, populate textures arg determines which layers are populated (1, 2, 3, 4, 5, 6, 7, 8)\n" +
                    " - ie: to populate all texture layers except layer 7, use: --textures 1234568\n" +
                    " - texture  layers 1-8 are: 1 Accent Ground, 2 Accent Plateaus, 3 Slopes, 4 Accent Slopes, 5 Steep Hills, 6 Water/Beach, 7 Rock, 8 Accent Rock" +
                    "--keep-layer-0 arg     optional, populate where texture layer 0 is currently visible to replace layer number arg (1, 2, 3, 4, 5, 6, 7, 8)\n" +
                    "--decals               optional, populate decals\n" +
                    "--ai                   optional, populate ai markers\n" +
                    "--keep-current-decals  optional, prevents decals currently on the map from being deleted\n" +
                    "--debug                optional, turn on debugging options\n");
            System.exit(0);
        }

        if (arguments.containsKey("debug")) {
            DEBUG = true;
        }

        if (!arguments.containsKey("in-folder-path")) {
            System.out.println("Input Folder not Specified");
            System.exit(1);
        }

        if (!arguments.containsKey("out-folder-path")) {
            System.out.println("Output Folder not Specified");
            System.exit(2);
        }

        if (!arguments.containsKey("team-symmetry") || !arguments.containsKey("spawn-symmetry")) {
            System.out.println("Symmetries not Specified");
            System.exit(3);
        }

        inMapPath = Paths.get(arguments.get("in-folder-path"));
        outFolderPath = Paths.get(arguments.get("out-folder-path"));
        symmetryHierarchy = new SymmetryHierarchy(Symmetry.valueOf(arguments.get("spawn-symmetry")), Symmetry.valueOf(arguments.get("team-symmetry")));
        symmetryHierarchy.setSpawnSymmetry(Symmetry.valueOf(arguments.get("spawn-symmetry")));
        populateSpawns = arguments.containsKey("spawns");
        if (populateSpawns) {
            spawnCount = Integer.parseInt(arguments.get("spawns"));
        }
        populateMexes = arguments.containsKey("mexes");
        if (populateMexes) {
            mexCountPerPlayer = Integer.parseInt(arguments.get("mexes"));
        }
        populateHydros = arguments.containsKey("hydros");
        if (populateHydros) {
            hydroCountPerPlayer = Integer.parseInt(arguments.get("hydros"));
        }
        populateProps = arguments.containsKey("props");
        if (populateProps) {
            propsPath = Paths.get(arguments.get("props"));
        }
        populateTextures = arguments.containsKey("textures");
        if (populateTextures) {
            String whichTextures = arguments.get("textures");
            keepLayer1 = !whichTextures.contains("1");
            keepLayer2 = !whichTextures.contains("2");
            keepLayer3 = !whichTextures.contains("3");
            keepLayer4 = !whichTextures.contains("4");
            keepLayer5 = !whichTextures.contains("5");
            keepLayer6 = !whichTextures.contains("6");
            keepLayer7 = !whichTextures.contains("7");
            keepLayer8 = !whichTextures.contains("8");
        }
        keepLayer0 = arguments.containsKey("keep-layer-0");
        if (keepLayer0) {
            moveLayer0To = Integer.parseInt(arguments.get("keep-layer-0"));
        }
        populateDecals = arguments.containsKey("decals");
        populateAI = arguments.containsKey("ai");
        keepCurrentDecals = arguments.containsKey("keep-current-decals");
    }

    public void importMap() {
        try {
            File dir = inMapPath.toFile();

            File[] mapFiles = dir.listFiles((dir1, filename) -> filename.endsWith(".scmap"));
            if (mapFiles == null || mapFiles.length == 0) {
                System.out.println("No scmap file in map folder");
                return;
            }
            File scmapFile = mapFiles[0];
            mapFolder = inMapPath.getFileName().toString();
            mapName = scmapFile.getName().replace(".scmap", "");
            map = SCMapImporter.loadSCMAP(inMapPath);
            SaveImporter.importSave(inMapPath, map);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while saving the map.");
        }
    }

    public void exportMap() {
        try {
            long startTime = System.currentTimeMillis();
            FileUtils.copyRecursiveIfExists(inMapPath, outFolderPath);
            SCMapExporter.exportSCMAP(outFolderPath.resolve(mapFolder), mapName, map);
            SaveExporter.exportSave(outFolderPath.resolve(mapFolder), mapName, map);
            ScenarioExporter.exportScenario(outFolderPath.resolve(mapFolder), mapName, map);
            Files.copy(inMapPath.resolve(mapName + "_script.lua"), outFolderPath.resolve(mapFolder).resolve(mapName + "_script.lua"), StandardCopyOption.REPLACE_EXISTING);
            System.out.printf("File export done: %d ms\n", System.currentTimeMillis() - startTime);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error while saving the map.");
        }
    }

    public void populate() {
        Random random = new Random();
        boolean waterPresent = map.getBiome().getWaterSettings().isWaterPresent();
        FloatMask heightmapBase = map.getHeightMask(symmetryHierarchy);
        heightmapBase.applySymmetry();
        map.setHeightImage(heightmapBase);
        float waterHeight;
        if (waterPresent) {
            waterHeight = map.getBiome().getWaterSettings().getElevation();
        } else {
            waterHeight = heightmapBase.getMin();
        }

        BinaryMask land = new BinaryMask(heightmapBase, waterHeight, random.nextLong());
        BinaryMask plateaus = new BinaryMask(heightmapBase, waterHeight + 3f, random.nextLong());
        FloatMask slope = new FloatMask(heightmapBase, random.nextLong()).gradient();
        BinaryMask impassable = new BinaryMask(slope, .9f, random.nextLong());
        BinaryMask ramps = new BinaryMask(slope, .25f, random.nextLong()).minus(impassable);
        BinaryMask passable = impassable.copy().invert();
        BinaryMask passableLand = new BinaryMask(land, null);
        BinaryMask passableWater = new BinaryMask(land, null).invert();

        if (populateSpawns) {
            if (spawnCount > 0) {
                map.setSpawnCountInit(spawnCount);
                SpawnGenerator spawnGenerator = new SpawnGenerator(map, random.nextLong(), 48);
                float spawnSeparation = StrictMath.max(random.nextInt(map.getSize() / 4 - map.getSize() / 16) + map.getSize() / 16, 24);
                BinaryMask spawns = land.copy();
                spawns.intersect(passable).minus(ramps).deflate(16);
                spawnGenerator.generateSpawns(spawns, spawnSeparation);
                spawnGenerator.setMarkerHeights();
            } else {
                map.getSpawns().clear();
            }
        }

        if (populateMexes || populateHydros) {
            resourceMask = new BinaryMask(land, random.nextLong());
            waterResourceMask = new BinaryMask(land, random.nextLong()).invert();
            plateauResourceMask = new BinaryMask(land, random.nextLong());

            resourceMask.minus(impassable).deflate(8).minus(ramps);
            resourceMask.trimEdge(16).fillCenter(16, false);
            waterResourceMask.minus(ramps).deflate(16).trimEdge(16).fillCenter(16, false);
            plateauResourceMask.combine(resourceMask).intersect(plateaus).trimEdge(16).fillCenter(16, true);
        }

        if (populateMexes) {
            if (mexCountPerPlayer > 0) {
                map.setMexCountInit(mexCountPerPlayer * map.getSpawnCount());
                MexGenerator mexGenerator = new MexGenerator(map, random.nextLong(), 48, map.getSize() / 8);

                mexGenerator.generateMexes(resourceMask, plateauResourceMask, waterResourceMask);
                mexGenerator.setMarkerHeights();
            } else {
                map.getMexes().clear();
            }
        }

        if (populateHydros) {
            if (hydroCountPerPlayer > 0) {
                map.setMexCountInit(hydroCountPerPlayer * map.getSpawnCount());
                HydroGenerator hydroGenerator = new HydroGenerator(map, random.nextLong(), 48);

                hydroGenerator.generateHydros(resourceMask.deflate(4));
                hydroGenerator.setMarkerHeights();
            } else {
                map.getHydros().clear();
            }
        }

        if (populateTextures) {

            FloatMask[] texturesMasks = map.getTextureMasks(symmetryHierarchy);
            oldLayer1 = texturesMasks[0];
            oldLayer2 = texturesMasks[1];
            oldLayer3 = texturesMasks[2];
            oldLayer4 = texturesMasks[3];
            oldLayer5 = texturesMasks[4];
            oldLayer6 = texturesMasks[5];
            oldLayer7 = texturesMasks[6];
            oldLayer8 = texturesMasks[7];
            FloatMask oldLayer0 = new FloatMask(map.getSize() / 2, random.nextLong(), symmetryHierarchy);

            map.setTextureMasksLow(new BufferedImage(map.getSize() / 2, map.getSize() / 2, BufferedImage.TYPE_INT_ARGB));
            map.setTextureMasksHigh(new BufferedImage(map.getSize() / 2, map.getSize() / 2, BufferedImage.TYPE_INT_ARGB));

            oldLayer1.setSize(map.getSize()/2);
            oldLayer2.setSize(map.getSize()/2);
            oldLayer3.setSize(map.getSize()/2);
            oldLayer4.setSize(map.getSize()/2);
            oldLayer5.setSize(map.getSize()/2);
            oldLayer6.setSize(map.getSize()/2);
            oldLayer7.setSize(map.getSize()/2);
            oldLayer8.setSize(map.getSize()/2);
            
            BinaryMask flat = new BinaryMask(slope, .05f, random.nextLong()).invert();
            BinaryMask inland = new BinaryMask(land, random.nextLong());
            BinaryMask highGround = new BinaryMask(heightmapBase, waterHeight + 3f, random.nextLong());
            BinaryMask aboveBeach = new BinaryMask(heightmapBase, waterHeight + 1.5f, random.nextLong());
            BinaryMask aboveBeachEdge = new BinaryMask(heightmapBase, waterHeight + 3f, random.nextLong());
            BinaryMask flatAboveCoast = new BinaryMask(heightmapBase, waterHeight + 0.29f, random.nextLong());
            BinaryMask higherFlatAboveCoast = new BinaryMask(heightmapBase, waterHeight + 1.2f, random.nextLong());
            BinaryMask lowWaterBeach = new BinaryMask(heightmapBase, waterHeight, random.nextLong());
            BinaryMask waterBeach = new BinaryMask(heightmapBase, waterHeight + 1f, random.nextLong());
            BinaryMask accentGround = new BinaryMask(land, random.nextLong());
            BinaryMask accentPlateau = new BinaryMask(plateaus, random.nextLong());
            BinaryMask slopes = new BinaryMask(slope, .1f, random.nextLong());
            BinaryMask accentSlopes = new BinaryMask(slope, .75f, random.nextLong()).invert();
            BinaryMask steepHills = new BinaryMask(slope, .55f, random.nextLong());
            BinaryMask rock = new BinaryMask(slope, 1.25f, random.nextLong());
            BinaryMask accentRock = new BinaryMask(slope, 1.25f, random.nextLong());
            FloatMask waterBeachTexture = new FloatMask(map.getSize() / 2, random.nextLong(), symmetryHierarchy);
            FloatMask accentGroundTexture = new FloatMask(map.getSize() / 2, random.nextLong(), symmetryHierarchy);
            FloatMask accentPlateauTexture = new FloatMask(map.getSize() / 2, random.nextLong(), symmetryHierarchy);
            FloatMask slopesTexture = new FloatMask(map.getSize() / 2, random.nextLong(), symmetryHierarchy);
            FloatMask accentSlopesTexture = new FloatMask(map.getSize() / 2, random.nextLong(), symmetryHierarchy);
            FloatMask steepHillsTexture = new FloatMask(map.getSize() / 2, random.nextLong(), symmetryHierarchy);
            FloatMask rockTexture = new FloatMask(map.getSize() / 2, random.nextLong(), symmetryHierarchy);
            FloatMask accentRockTexture = new FloatMask(map.getSize() / 2, random.nextLong(), symmetryHierarchy);

            inland.deflate(2);
            flatAboveCoast.intersect(flat);
            higherFlatAboveCoast.intersect(flat);
            lowWaterBeach.invert().inflate(6).minus(aboveBeach);
            if (waterPresent) {
                waterBeach.invert().minus(flatAboveCoast).minus(inland).inflate(1).combine(lowWaterBeach).smooth(5, 0.5f).minus(aboveBeach).minus(higherFlatAboveCoast).smooth(2).smooth(1);
            } else {
                waterBeach.clear();
            }
            accentGround.minus(highGround).acid(.05f, 0).erode(.85f, symmetryHierarchy.getSpawnSymmetry()).smooth(2, .75f).acid(.45f, 0);
            accentPlateau.acid(.05f, 0).erode(.85f, symmetryHierarchy.getSpawnSymmetry()).smooth(2, .75f).acid(.45f, 0);
            slopes.intersect(land).flipValues(.95f).erode(.5f, symmetryHierarchy.getSpawnSymmetry()).acid(.3f, 0).erode(.2f, symmetryHierarchy.getSpawnSymmetry());
            accentSlopes.minus(flat).intersect(land).acid(.1f, 0).erode(.5f, symmetryHierarchy.getSpawnSymmetry()).smooth(4, .75f).acid(.55f, 0);
            steepHills.acid(.3f, 0).erode(.2f, symmetryHierarchy.getSpawnSymmetry());
            accentRock.acid(.2f, 0).erode(.3f, symmetryHierarchy.getSpawnSymmetry()).acid(.2f, 0).smooth(2, .5f).intersect(rock);

            waterBeachTexture.init(waterBeach,0,1).subtract(rock, 1f).subtract(aboveBeachEdge,1f).clampMin(0).smooth(2, rock.copy().invert()).add(waterBeach, 1f).subtract(rock, 1f);
            waterBeachTexture.subtract(aboveBeachEdge,.9f).clampMin(0).smooth(2, rock.copy().invert()).subtract(rock, 1f).subtract(aboveBeachEdge,.8f).clampMin(0).add(waterBeach, .65f).smooth(2, rock.copy().invert());
            waterBeachTexture.subtract(rock, 1f).subtract(aboveBeachEdge,0.7f).clampMin(0).add(waterBeach, .5f).smooth(2, rock.copy().invert()).smooth(2, rock.copy().invert()).subtract(rock, 1f).clampMin(0).smooth(2, rock.copy().invert());
            waterBeachTexture.smooth(2, rock.copy().invert()).subtract(rock, 1f).clampMin(0).smooth(2, rock.copy().invert()).smooth(1, rock.copy().invert()).smooth(1, rock.copy().invert()).clampMax(1f);
            accentGroundTexture.init(accentGround, 0, 1).smooth(8).add(accentGround, .65f).smooth(4).add(accentGround, .5f).smooth(1).clampMax(1f);
            accentPlateauTexture.init(accentPlateau, 0, 1).smooth(8).add(accentPlateau, .65f).smooth(4).add(accentPlateau, .5f).smooth(1).clampMax(1f);
            slopesTexture.init(slopes, 0, 1).smooth(8).add(slopes, .65f).smooth(4).add(slopes, .5f).smooth(1).clampMax(1f);
            accentSlopesTexture.init(accentSlopes, 0, 1).smooth(8).add(accentSlopes, .65f).smooth(4).add(accentSlopes, .5f).smooth(1).clampMax(1f);
            steepHillsTexture.init(steepHills, 0, 1).smooth(8).clampMax(0.35f).add(steepHills, .65f).smooth(4).clampMax(0.65f).add(steepHills, .5f).smooth(1).add(steepHills, 1f).clampMax(1f);
            rockTexture.init(rock, 0, 1).smooth(8).clampMax(0.2f).add(rock, .65f).smooth(4).clampMax(0.3f).add(rock, .5f).smooth(1).add(rock, 1f).clampMax(1f);
            accentRockTexture.init(accentRock, 0, 1).subtract(waterBeachTexture).clampMin(0).smooth(8).add(accentRock, .65f).smooth(4).add(accentRock, .5f).smooth(1).clampMax(1f);

            if (keepLayer1) {
                accentGroundTexture.replaceWith(oldLayer1);
            }
            if (keepLayer2) {
                accentPlateauTexture.replaceWith(oldLayer2);
            }
            if (keepLayer3) {
                slopesTexture.replaceWith(oldLayer3);
            }
            if (keepLayer4) {
                accentSlopesTexture.replaceWith(oldLayer4);
            }
            if (keepLayer5) {
                steepHillsTexture.replaceWith(oldLayer5);
            }
            if (keepLayer6) {
                waterBeachTexture.replaceWith(oldLayer6);
            }
            if (keepLayer7) {
                rockTexture.replaceWith(oldLayer7);
            }
            if (keepLayer8) {
                accentRockTexture.replaceWith(oldLayer8);
            }
            if (keepLayer0) {
                oldLayer0.subtract(oldLayer8).subtract(oldLayer7).subtract(oldLayer6).subtract(oldLayer5).subtract(oldLayer4).subtract(oldLayer3).subtract(oldLayer2).subtract(oldLayer1);
                if (moveLayer0To == 1) {
                    accentGroundTexture.replaceWith(oldLayer0);
                } else if (moveLayer0To == 2) {
                    accentPlateauTexture.replaceWith(oldLayer0);
                } else if (moveLayer0To == 3) {
                    slopesTexture.replaceWith(oldLayer0);
                } else if (moveLayer0To == 4) {
                    accentSlopesTexture.replaceWith(oldLayer0);
                } else if (moveLayer0To == 5) {
                    steepHillsTexture.replaceWith(oldLayer0);
                } else if (moveLayer0To == 6) {
                    waterBeachTexture.replaceWith(oldLayer0);
                } else if (moveLayer0To == 7) {
                    rockTexture.replaceWith(oldLayer0);
                } else if (moveLayer0To == 8) {
                    accentRockTexture.replaceWith(oldLayer0);
                }
            }

            map.setTextureMasksLow(accentGroundTexture, accentPlateauTexture, slopesTexture, accentSlopesTexture);
            map.setTextureMasksHigh(steepHillsTexture, waterBeachTexture, rockTexture, accentRockTexture);
        }

        if (populateProps) {
            map.getProps().clear();
            PropGenerator propGenerator = new PropGenerator(map, random.nextLong());
            PropMaterials propMaterials = null;

            try {
                propMaterials = FileUtils.deserialize(propsPath.getParent(), propsPath.getFileName().toString(), PropMaterials.class);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.print("An error occured while loading props\n");
                System.exit(1);
            }
            BinaryMask treeMask = new BinaryMask(map.getSize() / 16, random.nextLong(), symmetryHierarchy);
            BinaryMask cliffRockMask = new BinaryMask(map.getSize() / 16, random.nextLong(), symmetryHierarchy);
            BinaryMask fieldStoneMask = new BinaryMask(map.getSize() / 4, random.nextLong(), symmetryHierarchy);
            BinaryMask largeRockFieldMask = new BinaryMask(map.getSize() / 4, random.nextLong(), symmetryHierarchy);
            BinaryMask smallRockFieldMask = new BinaryMask(map.getSize() / 4, random.nextLong(), symmetryHierarchy);

            cliffRockMask.randomize(.4f).intersect(impassable).grow(.5f, symmetryHierarchy.getSpawnSymmetry(), 4).minus(plateaus.copy().outline()).intersect(land);
            fieldStoneMask.randomize(random.nextFloat() * .001f).enlarge(256).intersect(land).minus(impassable);
            fieldStoneMask.enlarge(map.getSize() + 1).trimEdge(10);
            treeMask.randomize(.2f).enlarge(map.getSize() / 4).inflate(2).erode(.5f, symmetryHierarchy.getSpawnSymmetry()).smooth(4, .75f).erode(.5f, symmetryHierarchy.getSpawnSymmetry());
            treeMask.enlarge(map.getSize() + 1).intersect(land.copy().deflate(8)).minus(impassable.copy().inflate(2)).deflate(2).trimEdge(8).smooth(4, .25f);
            largeRockFieldMask.randomize(random.nextFloat() * .001f).trimEdge(map.getSize() / 16).grow(.5f, symmetryHierarchy.getSpawnSymmetry(), 3).intersect(land).minus(impassable);
            smallRockFieldMask.randomize(random.nextFloat() * .003f).trimEdge(map.getSize() / 64).grow(.5f, symmetryHierarchy.getSpawnSymmetry()).intersect(land).minus(impassable);

            BinaryMask noProps = new BinaryMask(impassable, null);

            for (int i = 0; i < map.getSpawnCount(); i++) {
                noProps.fillCircle(map.getSpawn(i), 30, true);
            }
            for (int i = 0; i < map.getMexCount(); i++) {
                noProps.fillCircle(map.getMex(i), 10, true);
            }
            for (int i = 0; i < map.getHydroCount(); i++) {
                noProps.fillCircle(map.getHydro(i), 16, true);
            }

            if (propMaterials.getTreeGroups() != null && propMaterials.getTreeGroups().length > 0) {
                propGenerator.generateProps(treeMask.minus(noProps), propMaterials.getTreeGroups(), 3f);
            }
            if (propMaterials.getRocks() != null && propMaterials.getRocks().length > 0) {
                propGenerator.generateProps(cliffRockMask.minus(noProps), propMaterials.getRocks(), 1.5f);
                propGenerator.generateProps(largeRockFieldMask.minus(noProps), propMaterials.getRocks(), 1.5f);
                propGenerator.generateProps(smallRockFieldMask.minus(noProps), propMaterials.getRocks(), 1.5f);
            }
            if (propMaterials.getBoulders() != null && propMaterials.getBoulders().length > 0) {
                propGenerator.generateProps(fieldStoneMask.minus(noProps), propMaterials.getBoulders(), 30f);
            }

            propGenerator.setPropHeights();
        }

        if (populateDecals) {
            if (!keepCurrentDecals) {
                map.getDecals().clear();
            }
            DecalGenerator decalGenerator = new DecalGenerator(map, random.nextLong());

            BinaryMask intDecal = new BinaryMask(land, random.nextLong());
            BinaryMask rockDecal = new BinaryMask(slope, 1.25f, random.nextLong());

            BinaryMask noDecals = new BinaryMask(map.getSize() + 1, null, symmetryHierarchy);

            for (int i = 0; i < map.getSpawnCount(); i++) {
                noDecals.fillCircle(map.getSpawn(i), 24, true);
            }

            decalGenerator.generateDecals(intDecal.minus(noDecals), DecalGenerator.INT, 96f, 64f);
            decalGenerator.generateDecals(rockDecal.minus(noDecals), DecalGenerator.ROCKS, 8f, 16f);
        }

        if (populateAI) {
            BinaryMask passableAI = passable.copy().deflate(6).trimEdge(8);
            passableLand.deflate(4).intersect(passableAI);
            passableWater.deflate(16).trimEdge(8);
            AIMarkerGenerator aiMarkerGenerator = new AIMarkerGenerator(map, 0);
            aiMarkerGenerator.generateAIMarkers(passableAI, passableLand, passableWater, 8, 16, false);
            aiMarkerGenerator.setMarkerHeights();
        }
    }
}
