package exporter;

import map.SCMap;
import util.Vector3f;

import java.io.*;
import java.nio.file.Path;

public strictfp class SaveExporter {

    public static File file;
    private static DataOutputStream out;

    public static void exportSave(Path folderPath, String mapname, SCMap map) throws IOException {
        file = folderPath.resolve(mapname + "_save.lua").toFile();
        boolean status = file.createNewFile();
        out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        out.writeBytes("Scenario = {\n");
        out.writeBytes("  next_area_id = '1',\n");
        out.writeBytes("  Props = {},\n");
        out.writeBytes("  Areas = {},\n");
        out.writeBytes("  MasterChain = {\n");
        out.writeBytes("    ['_MASTERCHAIN_'] = {\n");
        out.writeBytes("      Markers = {\n");
        for (int i = 0; i < map.getSpawnCount(); i++) {
            out.writeBytes("        ['ARMY_" + map.getSpawn(i).getId() + "'] = {\n");
            out.writeBytes("          ['type'] = STRING( 'Blank Marker' ),\n");
            out.writeBytes("          ['position'] = VECTOR3( " + (map.getSpawn(i).getPosition().x) + ", " + map.getSpawn(i).getPosition().y + ", " + (map.getSpawn(i).getPosition().z) + " ),\n");
            out.writeBytes("          ['orientation'] = VECTOR3( 0.00, 0.00, 0.00 ),\n");
            out.writeBytes("          ['color'] = STRING( 'ff800080' ),\n");
            out.writeBytes("          ['prop'] = STRING( '/env/common/props/markers/M_Blank_prop.bp' ),\n");
            out.writeBytes("        },\n");
        }
        for (int i = 0; i < map.getMexCount(); i++) {
            out.writeBytes("        ['MASS_" + (i + 1) + "'] = {\n");
            out.writeBytes("          ['size'] = FLOAT( 1.000000 ),\n");
            out.writeBytes("          ['resource'] = BOOLEAN( true ),\n");
            out.writeBytes("          ['amount'] = FLOAT( 100.000000 ),\n");
            out.writeBytes("          ['color'] = STRING( 'ff808080' ),\n");
            out.writeBytes("          ['type'] = STRING( 'Mass' ),\n");
            out.writeBytes("          ['prop'] = STRING( '/env/common/props/markers/M_Mass_prop.bp' ),\n");
            out.writeBytes("          ['orientation'] = VECTOR3( 0, 0, 0 ),\n");
            out.writeBytes("          ['position'] = VECTOR3( " + (map.getMex(i).x) + ", " + map.getMex(i).y + ", " + (map.getMex(i).z) + " ),\n");
            out.writeBytes("        },\n");
        }
        for (int i = 0; i < map.getHydroCount(); i++) {
            out.writeBytes("        ['Hydrocarbon_" + (i + 1) + "'] = {\n");
            out.writeBytes("          ['size'] = FLOAT( 3.00 ),\n");
            out.writeBytes("          ['resource'] = BOOLEAN( true ),\n");
            out.writeBytes("          ['amount'] = FLOAT( 100.000000 ),\n");
            out.writeBytes("          ['color'] = STRING( 'ff808080' ),\n");
            out.writeBytes("          ['type'] = STRING( 'Hydrocarbon' ),\n");
            out.writeBytes("          ['prop'] = STRING( '/env/common/props/markers/M_Hydrocarbon_prop.bp' ),\n");
            out.writeBytes("          ['orientation'] = VECTOR3( 0, 0, 0 ),\n");
            out.writeBytes("          ['position'] = VECTOR3( " + (map.getHydro(i).x) + ", " + map.getHydro(i).y + ", " + (map.getHydro(i).z) + " ),\n");
            out.writeBytes("        },\n");
        }
        for (int i = 0; i < map.getAirMarkerCount(); i++) {
            if (map.getAirMarker(i).getNeighborCount() >= 0) {
                out.writeBytes("        ['AirPN" + map.getAirMarker(i).getId() + "'] = {\n");
                out.writeBytes("          ['hint'] = BOOLEAN( true ),\n");
                out.writeBytes("          ['type'] = STRING( 'Air Path Node' ),\n");
                out.writeBytes("          ['adjacentTo'] = STRING( '");
                map.getAirMarker(i).getNeighbors().forEach(id -> {
                    try {
                        out.writeBytes(" AirPN" + id);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                out.writeBytes(" '),\n");
                out.writeBytes("          ['color'] = STRING( 'ffffffff' ),\n");
                out.writeBytes("          ['graph'] = STRING( 'DefaultAir' ),\n");
                out.writeBytes("          ['prop'] = STRING( '/env/common/props/markers/M_Path_prop.bp' ),\n");
                out.writeBytes("          ['orientation'] = VECTOR3( 0, 0, 0 ),\n");
                out.writeBytes("          ['position'] = VECTOR3( " + (map.getAirMarker(i).getPosition().x) + ", " + map.getAirMarker(i).getPosition().y + ", " + (map.getAirMarker(i).getPosition().z) + " ),\n");
                out.writeBytes("        },\n");
            }
        }
        for (int i = 0; i < map.getLandMarkerCount(); i++) {
            if (map.getLandMarker(i).getNeighborCount() >= 0) {
                out.writeBytes("        ['LandPN" + map.getLandMarker(i).getId() + "'] = {\n");
                out.writeBytes("          ['hint'] = BOOLEAN( true ),\n");
                out.writeBytes("          ['type'] = STRING( 'Land Path Node' ),\n");
                out.writeBytes("          ['adjacentTo'] = STRING( '");
                map.getLandMarker(i).getNeighbors().forEach(id -> {
                    try {
                        out.writeBytes(" LandPN" + id);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                out.writeBytes(" '),\n");
                out.writeBytes("          ['color'] = STRING( 'ff00ff00' ),\n");
                out.writeBytes("          ['graph'] = STRING( 'DefaultLand' ),\n");
                out.writeBytes("          ['prop'] = STRING( '/env/common/props/markers/M_Path_prop.bp' ),\n");
                out.writeBytes("          ['orientation'] = VECTOR3( 0, 0, 0 ),\n");
                out.writeBytes("          ['position'] = VECTOR3( " + (map.getLandMarker(i).getPosition().x) + ", " + map.getLandMarker(i).getPosition().y + ", " + (map.getLandMarker(i).getPosition().z) + " ),\n");
                out.writeBytes("        },\n");
            }
        }
        for (int i = 0; i < map.getAmphibiousMarkerCount(); i++) {
            if (map.getAmphibiousMarker(i).getNeighborCount() >= 0) {
                out.writeBytes("        ['AmphPN" + map.getAmphibiousMarker(i).getId() + "'] = {\n");
                out.writeBytes("          ['hint'] = BOOLEAN( true ),\n");
                out.writeBytes("          ['type'] = STRING( 'Amphibious Path Node' ),\n");
                out.writeBytes("          ['adjacentTo'] = STRING( '");
                map.getAmphibiousMarker(i).getNeighbors().forEach(id -> {
                    try {
                        out.writeBytes(" AmphPN" + id);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                out.writeBytes(" '),\n");
                out.writeBytes("          ['color'] = STRING( 'ff00ffff' ),\n");
                out.writeBytes("          ['graph'] = STRING( 'DefaultAmphibious' ),\n");
                out.writeBytes("          ['prop'] = STRING( '/env/common/props/markers/M_Path_prop.bp' ),\n");
                out.writeBytes("          ['orientation'] = VECTOR3( 0, 0, 0 ),\n");
                out.writeBytes("          ['position'] = VECTOR3( " + (map.getAmphibiousMarker(i).getPosition().x) + ", " + map.getAmphibiousMarker(i).getPosition().y + ", " + (map.getAmphibiousMarker(i).getPosition().z) + " ),\n");
                out.writeBytes("        },\n");
            }
        }
        for (int i = 0; i < map.getNavyMarkerCount(); i++) {
            if (map.getNavyMarker(i).getNeighborCount() >= 0) {
                out.writeBytes("        ['WaterPN" + map.getNavyMarker(i).getId() + "'] = {\n");
                out.writeBytes("          ['hint'] = BOOLEAN( true ),\n");
                out.writeBytes("          ['type'] = STRING( 'Water Path Node' ),\n");
                out.writeBytes("          ['adjacentTo'] = STRING( '");
                map.getNavyMarker(i).getNeighbors().forEach(id -> {
                    try {
                        out.writeBytes(" WaterPN" + id);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                out.writeBytes(" '),\n");
                out.writeBytes("          ['color'] = STRING( 'ff0000ff' ),\n");
                out.writeBytes("          ['graph'] = STRING( 'DefaultWater' ),\n");
                out.writeBytes("          ['prop'] = STRING( '/env/common/props/markers/M_Path_prop.bp' ),\n");
                out.writeBytes("          ['orientation'] = VECTOR3( 0, 0, 0 ),\n");
                out.writeBytes("          ['position'] = VECTOR3( " + (map.getNavyMarker(i).getPosition().x) + ", " + map.getNavyMarker(i).getPosition().y + ", " + (map.getNavyMarker(i).getPosition().z) + " ),\n");
                out.writeBytes("        },\n");
            }
        }
        for (int i = 0; i < map.getLargeExpansionMarkerCount(); i++) {
            out.writeBytes("        ['Large Expansion Area " + i + "'] = {\n");
            out.writeBytes("          ['hint'] = BOOLEAN( true ),\n");
            out.writeBytes("          ['color'] = STRING( 'ffff0080' ),\n");
            out.writeBytes("          ['type'] = STRING( 'Large Expansion Area' ),\n");
            out.writeBytes("          ['prop'] = STRING( '/env/common/props/markers/M_Expansion_prop.bp' ),\n");
            out.writeBytes("          ['orientation'] = VECTOR3( 0, 0, 0 ),\n");
            out.writeBytes("          ['position'] = VECTOR3( " + (map.getLargeExpansionMarker(i).getPosition().x) + ", " + map.getLargeExpansionMarker(i).getPosition().y + ", " + (map.getLargeExpansionMarker(i).getPosition().z) + " ),\n");
            out.writeBytes("        },\n");
        }
        for (int i = 0; i < map.getExpansionMarkerCount(); i++) {
            out.writeBytes("        ['Expansion Area " + i + "'] = {\n");
            out.writeBytes("          ['hint'] = BOOLEAN( true ),\n");
            out.writeBytes("          ['color'] = STRING( 'ff008080' ),\n");
            out.writeBytes("          ['type'] = STRING( 'Expansion Area' ),\n");
            out.writeBytes("          ['prop'] = STRING( '/env/common/props/markers/M_Expansion_prop.bp' ),\n");
            out.writeBytes("          ['orientation'] = VECTOR3( 0, 0, 0 ),\n");
            out.writeBytes("          ['position'] = VECTOR3( " + (map.getExpansionMarker(i).getPosition().x) + ", " + map.getExpansionMarker(i).getPosition().y + ", " + (map.getExpansionMarker(i).getPosition().z) + " ),\n");
            out.writeBytes("        },\n");
        }
        out.writeBytes("      },\n");
        out.writeBytes("    },\n");
        out.writeBytes("  },\n");
        out.writeBytes("  Chains = {},\n");
        out.writeBytes("  next_queue_id = '1',\n");
        out.writeBytes("  Orders = {},\n");
        out.writeBytes("  next_platoon_id = '1',\n");
        out.writeBytes("  Platoons = {},\n");
        out.writeBytes("  next_army_id = '1',\n");
        out.writeBytes("  next_group_id = '1',\n");
        out.writeBytes("  next_unit_id = '1',\n");
        out.writeBytes("  Armies = {\n");
        for (int i = 0; i < map.getSpawnCount(); i++) {
            saveArmy("ARMY_" + (i + 1), map);
        }
        saveArmy("ARMY_17", map);
        saveArmy("NEUTRAL_CIVILIAN", map);
        out.writeBytes("  },\n");
        out.writeBytes("}\n");

        out.flush();
        out.close();
    }

    private static void saveArmy(String name, SCMap map) throws IOException {
        out.writeBytes("    ['" + name + "'] = {\n");
        out.writeBytes("      personality = '',\n");
        out.writeBytes("      plans = '',\n");
        out.writeBytes("      color = 0,\n");
        out.writeBytes("      faction = 0,\n");
        out.writeBytes("      Economy = {mass = 0, energy = 0},\n");
        out.writeBytes("      Alliances = {},\n");
        out.writeBytes("      ['Units'] = GROUP {\n");
        out.writeBytes("        orders = '',\n");
        out.writeBytes("        platoon = '',\n");
        if (name.equals("ARMY_17")) {
            out.writeBytes("        Units = {\n");
            out.writeBytes("          ['INITIAL'] = GROUP {\n");
            out.writeBytes("            orders = '',\n");
            out.writeBytes("            platoon = '',\n");
            out.writeBytes("            Units = {\n");
            for (int i = 0; i < map.getUnitCount(); i++) {
                saveUnit(map.getUnit(i), i);
            }
            out.writeBytes("            },\n");
            out.writeBytes("          },\n");
            out.writeBytes("          ['WRECKAGE'] = GROUP {\n");
            out.writeBytes("            orders = '',\n");
            out.writeBytes("            platoon = '',\n");
            out.writeBytes("            Units = {\n");
            for (int i = 0; i < map.getWreckCount(); i++) {
                saveUnit(map.getWreck(i), i);
            }
            out.writeBytes("            },\n");
            out.writeBytes("          },\n");
        } else if (name.equals("NEUTRAL_CIVILIAN")) {
            out.writeBytes("        Units = {\n");
            out.writeBytes("          ['INITIAL'] = GROUP {\n");
            out.writeBytes("            orders = '',\n");
            out.writeBytes("            platoon = '',\n");
            out.writeBytes("            Units = {\n");
            for (int i = 0; i < map.getCivCount(); i++) {
                saveUnit(map.getCiv(i), i);
            }
            out.writeBytes("            },\n");
            out.writeBytes("          },\n");
        } else {
            out.writeBytes("        Units = {\n");
            out.writeBytes("          ['INITIAL'] = GROUP {\n");
            out.writeBytes("            orders = '',\n");
            out.writeBytes("            platoon = '',\n");
            out.writeBytes("            Units = {\n");
            out.writeBytes("            },\n");
            out.writeBytes("          },\n");
        }
        out.writeBytes("        },\n");
        out.writeBytes("      },\n");
        out.writeBytes("      PlatoonBuilders = {\n");
        out.writeBytes("        next_platoon_builder_id = '0',\n");
        out.writeBytes("        Builders = {},\n");
        out.writeBytes("      },\n");
        out.writeBytes("    },\n");
    }

    private static void saveUnit(map.Unit unit, int i) throws IOException {
        out.writeBytes(String.format("              ['UNIT_%d'] = {\n", i));
        out.writeBytes(String.format("	              type = '%s',\n", unit.getType()));
        out.writeBytes("			              orders = '',\n");
        out.writeBytes("			              platoon = '',\n");
        Vector3f v = unit.getPosition();
        out.writeBytes(String.format("			              Position = { %f, %f, %f },\n", v.x, v.y, v.z));
        float rot = unit.getRotation();
        out.writeBytes(String.format("			              Orientation = { 0, %f, 0 },\n", rot));
        out.writeBytes("              },\n");
    }
}
