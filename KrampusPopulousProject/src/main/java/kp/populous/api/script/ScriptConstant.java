/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kp.populous.api.script;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import kp.populous.api.data.UInt16;
import kp.populous.api.data.UInt32;

/**
 *
 * @author Asus
 */
public final class ScriptConstant
{
    public static final int SCRIPT_VERSION = 12;
    public static final int NO_COMMANDS = 27;
    public static final int TOKEN_OFFSET = 1000;
    public static final int INT_OFFSET = 1000;
    public static final int MAX_CODES = 4096;
    public static final int MAX_FIELDS = 512;
    public static final int MAX_VARS = 64;
    
    
    public enum FieldType
    {
        CONSTANT(0),
        USER(1),
        INTERNAL(2),
        INVALID(3);
        
        private final UInt32 code;
        
        private FieldType(int code)
        {
            this.code = UInt32.valueOf(code);
        }
        
        public final UInt32 getCode() { return code; }
        
        public final boolean equalsCode(UInt32 code) { return this.code.equals(code); }
        
        public static final FieldType decode(UInt32 code)
        {
            switch(code.toInt())
            {
                case 0: return CONSTANT;
                case 1: return USER;
                case 2: return INTERNAL;
                default: return INVALID;
            }
        }
    }
    
    public enum Token
    {
        IF(0, false, false, "if"),
        ELSE(1, false, false, "else"),
        ENDIF(2, false, false, null),
        BEGIN(3, false, false, null),
        END(4, false, false, null),
        EVERY(5, false, false, "every"),
        DO(6, false, false, null),
        SET(7, false, false, "set"),
        INCREMENT(8, false, false, "inc"),
        DECREMENT(9, false, false, "dec"),
        EXP_START(10, false, false, "exp_start"),
        EXP_END(11, false, false, "exp_end"),
        GREATER_THAN(12, false, false, ">"),
        LESS_THAN(13, false, false, "<"),
        EQUAL_TO(14, false, false, "=="),
        NOT_EQUAL_TO(15, false, false, "!="),
        GREATER_THAN_EQUAL_TO(16, false, false, ">="),
        LESS_THAN_EQUAL_TO(17, false, false, ">="),
        SCRIPT_END(19, false, false, null),
        AND(20, false, false, "&&"),
        OR(21, false, false, "||"),
        ON(22, false, false, "on"),
        OFF(23, false, false, "off"),
        COMPUTER_PLAYER(24, false, false, "computer_player"),
        MULTIPLY(25, false, false, "mul"),
        DIVIDE(26, false, false, "div"),
        
        CONSTRUCT_BUILDING(1),
        FETCH_WOOD(2),
        SHAMAN_GET_WILDS(3),
        HOUSE_A_PERSON(4),
        SEND_GHOSTS(5),
        BRING_NEW_PEOPLE_BACK(6),
        TRAIN_PEOPLE(7),
        POPULATE_DRUM_TOWER(8),
        DEFEND(9),
        DEFEND_BASE(10),
        SPELL_DEFENSE(11),
        PREACH(12),
        BUILD_WALLS(13),
        SABOTAGE(14),
        SPELL_OFFENSIVE(15),
        FIREWARRIOR_DEFEND(16),
        BUILD_VEHICLE(17),
        FETCH_LOST_PEOPLE(18),
        FETCH_LOST_VEHICLE(19),
        FETCH_FAR_VEHICLE(20),
        AUTO_ATTACK(21),
        
        SHAMAN_DEFEND(22),
        FLATTEN_BASE(23),
        BUILD_OUTER_DEFENCES(24),
        SPARE5(25),
        SPARE6(26),
        SPARE7(27),
        SPARE8(28),
        SPARE9(29),
        SPARE10(30),
        COUNT_WILD(31, false, true, null),
        
        ATTACK(32),
        ATTACK_BLUE(33),
        ATTACK_RED(34),
        ATTACK_YELLOW(35),
        ATTACK_GREEN(36),
        SPELL_ATTACK(37),
        
        RESET_BASE_MARKER(38),
        SET_BASE_MARKER(39),
        SET_BASE_RADIUS(40),
        COUNT_PEOPLE_IN_MARKER(41),
        SET_DRUM_TOWER_POS(42),
        
        ATTACK_MARKER(43, false, true, null),
        ATTACK_BUILDING(44, false, true, null),
        ATTACK_PERSON(45, false, true, null),
        CONVERT_AT_MARKER(46),
        PREACH_AT_MARKER(47),
        SEND_GHOST_PEOPLE(48),
        GET_SPELLS_CAST(49),
        GET_NUM_ONE_OFF_SPELLS(50),
        ATTACK_NORMAL(51, false, true, null),
        ATTACK_BY_BOAT(52, false, true, null),
        ATTACK_BY_BALLON(53, false, true, null),
        SET_ATTACK_VARIABLE(54),
        BUILD_DRUM_TOWER(55),
        GUARD_AT_MARKER(56),
        GUARD_BETWEEN_MARKERS(57),
        GET_HEIGHT_AT_POS(58),
        SEND_ALL_PEOPLE_TO_MARKER(59),
        GUARD_NORMAL(60, false, true, null),
        GUARD_WITH_GHOSTS(61, false, true, null),
        RESET_CONVERT_MARKER(62),
        SET_CONVERT_MARKER(63),
        SET_MARKER_ENTRY(64),
        MARKER_ENTRIES(65),
        CLEAR_GUARDING_FROM(66),
        SET_BUILDING_DIRECTION(67),
        TRAIN_PEOPLE_NOW(68),
        PRAY_AT_HEAD(69),
        PUT_PERSON_IN_DT(70),
        I_HAVE_ONE_SHOT(71),
        SPELL_TYPE(72),
        BUILDING_TYPE(73),
        BOAT_PATROL(74),
        DEFEND_SHAMEN(75),
        SEND_SHAMEN_DEFENDERS_HOME(76),
        BOAT_TYPE(77),
        BALLON_TYPE(78),
        IS_BUILDING_NEAR(79),
        BUILD_AT(80),
        SET_SPELL_ENTRY(81),
        DELAY_MAIN_DRUM_TOWER(82),
        BUILD_MAIN_DRUM_TOWER(83),
        ZOOM_TO(84),
        DISABLE_USER_INPUTS(85),
        ENABLE_USER_INPUTS(86),
        OPEN_DIALOG(87),
        GIVE_ONE_SHOT(88),
        CLEAR_STANDING_PEOPLE(89),
        ONLY_STAND_AT_MARKERS(90),
        BLUE(91, false, true, null),
        RED(92, false, true, null),
        YELLOW(93, false, true, null),
        GREEN(94, false, true, null),
        NAV_CHECK(95),
        TARGET_S_WARRIORS(96),
        DONT_TARGET_S_WARRIORS(97),
        TARGET_BLUE_SHAMAN(98),
        DONT_TARGET_BLUE_SHAMAN(99),
        TARGET_BLUE_DRUM_TOWERS(100),
        DONT_TARGET_BLUE_DRUM_TOWERS(101),
        HAS_BLUE_KILLED_A_GHOST(102),
        COUNT_GUARD_FIRES(103),
        GET_HEAD_TRIGGER_COUNT(104),
        MOVE_SHAMAN_TO_MARKER(105),
        TRACK_SHAMAN_TO_ANGLE(106),
        TRACK_SHAMAN_EXTRA_BOLLOCKS(107),
        IS_SHAMAN_AVAILABLE_FOR_ATTACK(108),
        PARTIAL_BUILDING_COUNT(109),
        SEND_BLUE_PEOPLE_TO_MARKER(110),
        GIVE_MANA_TO_PLAYER(111),
        IS_PLAYER_IN_WORLD_VIEW(112),
        SET_AUTO_BUILD(113),
        DESELECT_ALL_BLUE_PEOPLE(114),
        FLASH_BUTTON(115),
        TURN_PANEL_ON(116),
        GIVE_PLAYER_SPELL(117),
        HAS_PLAYER_BEEN_IN_ENCYC(118),
        IS_BLUE_SHAMAN_SELECTED(119),
        CLEAR_SHAMAN_LEFT_CLICK(120),
        CLEAR_SHAMAN_RIGHT_CLICK(121),
        IS_SHAMAN_ICON_LEFT_CLICKED(122),
        IS_SHAMAN_ICON_RIGHT_CLICKED(123),
        TRIGGER_THING(124),
        TRACK_TO_MARKER(125),
        CAMERA_ROTATION(126),
        STOP_CAMERA_ROTATION(127),
        COUNT_BLUE_SHAPES(128),
        COUNT_BLUE_IN_HOUSES(129),
        HAS_HOUSE_INFO_BEEN_SHOWN(130),
        CLEAR_HOUSE_INFO_FLAG(131),
        SET_AUTO_HOUSE(132),
        COUNT_BLUE_WITH_BUILD_COMMAND(133),
        DONT_HOUSE_SPECIALISTS(134),
        TARGET_PLAYER_DT_AND_S(135),
        REMOVE_PLAYER_THING(136),
        SET_REINCARNATION(137),
        EXTRA_WOOD_COLLECTION(138),
        SET_WOOD_COLLECTION_RADII(139),
        GET_NUM_PEOPLE_CONVERTED(140),
        GET_NUM_PEOPLE_BEING_PREACHED(141),
        
        TRIGGER_LEVEL_LOST(142),
        TRIGGER_LEVEL_WON(143),
        
        REMOVE_HEAD_AT_POS(144),
        SET_BUCKET_USAGE(145),
        SET_BUCKET_COUNT_FOR_SPELL(146),
        CREATE_MSG_NARRATIVE(147),
        CREATE_MSG_OBJECTIVE(148),
        CREATE_MSG_INFORMATION(149),
        CREATE_MSG_INFORMATION_ZOON(150),
        SET_MSG_ZOON(151),
        SET_MSG_TIMEOUT(152),
        SET_MSG_DELETE_ON_OK(153),
        SET_MSG_RETURN_ON_OK(154),
        SET_MSG_DELETE_ON_RMB_ZOOM(155),
        SET_MSG_OPEN_DLG_ON_RMB_ZOOM(156),
        SET_MSG_CREATE_RETURN_MSG_ON_RMB_ZOOM(157),
        SET_MSG_OPEN_DLG_ON_RMB_DELETE(158),
        SET_MSG_ZOOM_ON_LMB_OPEN_DLG(159),
        SET_MSG_AUTO_OPEN_DLG(160),
        SET_SPECIAL_NO_BLDG_PANEL(161),
        SET_MSG_OK_SAVE_EXIT_DLG(162),
        FIX_WILD_IN_AREA(163),
        CHECK_IF_PERSON_PREACHED_TO(164),
        COUNT_ANGELS(165),
        SET_NO_BLUE_REINC(166),
        IS_SHAMAN_IN_AREA(167),
        FORCE_TOOLTIP(168),
        SET_DEFENCE_RADIUS(169),
        MARVELLOUS_HOUSE_DEATH(170),
        CALL_TO_ARMS(171),
        DELETE_SMOKE_STUFF(172),
        SET_TIMER_GOING(173),
        REMOVE_TIMER(174),
        HAS_TIMER_REACHED_ZERO(175),
        START_REINC_NOW(176),
        TURN_PUSH(177),
        FLYBY_CREATE_NEW(178),
        FLYBY_START(179),
        FLYBY_STOP(180),
        FLYBY_ALLOW_INTERRUPT(181),
        FLYBY_SET_EVENT_POS(182),
        FLYBY_SET_EVENT_ANGLE(183),
        FLYBY_SET_EVENT_ZOOM(184),
        FLYBY_SET_EVENT_INT_POINT(185),
        FLYBY_SET_EVENT_TOOLTIP(186),
        FLYBY_SET_END_TARGET(187),
        FLYBY_SET_MESSAGE(188),
        KILL_TEAM_IN_AREA(189),
        CLEAR_ALL_MSG(190),
        SET_MSG_ID(191),
        GET_MSG_ID(192),
        KILL_ALL_MSG_ID(193),
        GIVE_UP_AND_SULK(194),
        AUTO_MESSAGES(195),
        IS_PRISION_ON_LEVEL(196);
        
        private final UInt16 code;
        private final String function;
        private final String name;
        private final boolean useCommandNumber;
        
        private Token(int code, boolean isCommand, boolean useCommandNumber, String name)
        {
            this.code = UInt16.valueOf(TOKEN_OFFSET + (!useCommandNumber ? 0 : NO_COMMANDS) + code);
            function = !isCommand ? null : generateFunction();
            this.name = isCommand ? function : name == null ? name() : name;
            this.useCommandNumber = useCommandNumber;
        }
        private Token(int code) { this(code, true, true, null); }
        
        public final String getTokenName() { return name; }
        
        public final UInt16 getCode() { return code; }
        
        public final boolean equalsCode(UInt16 code) { return this.code.equals(code); }
        
        public final String getFunctionName() { return function; }
        
        public final boolean isFunction() { return function != null; }
        
        private String generateFunction()
        {
            String name0 = name();
            StringBuilder sb = new StringBuilder(name0.length());
            char[] chars = name0.toCharArray();
            boolean upper = false;
            for(int i=0;i<chars.length;i++)
            {
                char c = chars[i];
                if(i > 0)
                {
                    if(c == '_')
                    {
                        upper = true;
                        continue;
                    }
                    if(upper)
                    {
                        upper = false;
                        c = Character.toUpperCase(c);
                    }
                    else c = Character.toLowerCase(c);
                }
                sb.append(c);
            }
            return sb.toString();
        }
        
        private static final HashMap<UInt16, Token> CODES = new HashMap<>();
        private static final HashMap<String, Token> NAMES = new HashMap<>();
        static {
            for(Token token : values()) {
                CODES.put(token.code, token);
                NAMES.put(token.name, token);
            }
        }
        
        public static final Token decode(UInt16 code) 
        {
            return CODES.get(code);
        }
        
        public static final Token decode(String name) 
        {
            return NAMES.get(name);
        }
        
        @Override
        public final String toString() { return getTokenName(); }
        
        public static final String generateOnePerLineFunctions()
        {
            Token[] values = values();
            StringBuilder sb = new StringBuilder(values.length * 16);
            for(Token t : values)
                if(t.isFunction())
                    sb.append(t.getFunctionName()).append("\n");
            if(sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n')
                sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
        
        private static String getXmlCompletions()
        {
            StringBuilder sb = new StringBuilder();
            for(Token token : values())
            {
                if(!token.isFunction() && token.useCommandNumber)
                {
                    sb.append("<keyword name=\"").append(token.getTokenName())
                            .append("\" type=\"constant\" returnType=\"Token\">\n    <desc></desc>\n</keyword>\n");
                }
            }
            return sb.toString();
        }
    }
    
    
    
    
    public enum Internal
    {
        GAME_TURN(0, false),
        MY_NUM_PEOPLE(1, false),
        BLUE_PEOPLE(2, false),
        RED_PEOPLE(3, false),
        YELLOW_PEOPLE(4, false),
        GREEN_PEOPLE(5, false),
        MY_NUM_KILLED_BY_HUMAN(6, false),
        RED_RED_KILLED_BY_HUMAN(7, false),
        YELLOW_RED_KILLED_BY_HUMAN(8, false),
        GREEN_RED_KILLED_BY_HUMAN(9, false),
        WILD_PEOPLE(10, false),
        BLUE_MANA(11, false),
        RED_MANA(12, false),
        YELLOW_MANA(13, false),
        GREEN_MANA(14, false),
        
        ATTR_EXPANSION(0),
        ATTR_PREF_SPY_TRAINS(1),
        ATTR_PREF_RELIGIOUS_TRAINS(2),
        ATTR_PREF_WARRIOR_TRAINS(3),
        ATTR_PREF_FIREWARRIOR_TRAINS(4),
        ATTR_PREF_SPY_PEOPLE(5),
        ATTR_PREF_RELIGIOUS_PEOPLE(6),
        ATTR_PREF_WARRIOR_PEOPLE(7),
        ATTR_PREF_FIREWARRIOR_PEOPLE(8),
        ATTR_MAX_BUILDINGS_ON_GO(9),
        ATTR_HOUSE_PERCENTAGE(10),
        ATTR_AWAY_BRAVE(11),
        ATTR_AWAY_WARRIOR(12),
        ATTR_AWAY_RELIGIOUS(13),
        ATTR_DEFENSE_RAD_INCR(14),
        ATTR_MAX_DEFENSIVE_ACTIONS(15),
        ATTR_AWAY_SPY(16),
        ATTR_AWAY_FIREWARRIOR(17),
        ATTR_ATTACK_PERCENTAGE(18),
        ATTR_AWAY_SHAMAN(19),
        ATTR_PEOPLE_PER_BOAT(20),
        ATTR_PEOPLE_PER_BALLON(21),
        ATTR_DONT_USE_BOATS(22),
        ATTR_MAX_SPY_ATTACKS(23),
        ATTR_ENEMY_SPY_MAX_STAND(24),
        ATTR_MAX_ATTACKS(25),
        ATTR_EMPTY_AT_WAYPOING(26),
        ATTR_SPY_CHECK_FREQUENCY(27),
        ATTR_RETREAT_VALUE(28),
        ATTR_BASE_UNDER_ATTACK_RETREAT(29),
        ATTR_RANDOM_BUILD_SIDE(30),
        ATTR_USE_PREACHER_FOR_DEFENSE(31),
        ATTR_SHAMEN_BLAST(32),
        ATTR_MAX_TRAIN_AT_ONCE(33),
        ATTR_GROUP_OPTION(34),
        ATTR_PREF_BOAT_HUTS(35),
        ATTR_PREF_BALLON_HUTS(36),
        ATTR_PREF_BOAT_DRIVERS(37),
        ATTR_PREF_BALLON_DRIVERS(38),
        ATTR_FIGHT_STOP_DISTANCE(39),
        ATTR_SPY_DISCOVER_CHANGE(40),
        ATTR_COUNT_PREACH_DAMAGE(41),
        ATTR_DONT_GROUP_AT_DT(42),
        ATTR_SPELL_DELAY(43),
        ATTR_DONT_DELETE_USELESS_BOAT_HOUSE(44),
        ATTR_BOAT_HOUSE_BROKEN(45),
        ATTR_DONT_AUTO_TRAIN_PREACHERS(46),
        ATTR_SPARE_6(47),
        
        MY_MANA(48),
        
        M_SPEll_BURN_COST(49),
        M_SPEll_BLAST_COST(50),
        M_SPEll_LIGHTNING_COST(51),
        M_SPEll_WHIRLMIND_COST(52),
        M_SPEll_INSECT_PLAGUE_COST(53),
        M_SPEll_INVISIBILITY_COST(54),
        M_SPEll_HYPNOTISM_COST(55),
        M_SPEll_FIRESTORM_COST(56),
        M_SPEll_GHOST_ARMY_COST(57),
        M_SPEll_EROSION_COST(58),
        M_SPEll_SWAMP_COST(59),
        M_SPEll_LAND_BRIDGE_COST(60),
        M_SPEll_ANGEL_OF_DEAD_COST(61),
        M_SPEll_EARTHQUAKE_COST(62),
        M_SPEll_FLATTEN_COST(63),
        M_SPEll_VOLCANO_COST(64),
        M_SPEll_WRATH_OF_GOD_COST(65),
        
        M_BUILDING_TEPEE(66),
        M_BUILDING_HUT(67),
        M_BUILDING_FARM(68),
        M_BUILDING_DRUM_TOWER(69),
        M_BUILDING_TEMPLE(70),
        M_BUILDING_SPY_TRAIN(71),
        M_BUILDING_WARRIOR_TRAIN(72),
        M_BUILDING_FIREWARRIOR_TRAIN(73),
        M_BUILDING_RECONVERSION(74),
        M_BUILDING_WALL_PIECE(75),
        M_BUILDING_GATE(76),
        M_BUILDING_CURR_OE_SLOT(77),
        M_BUILDING_BOAT_HUT_1(78),
        M_BUILDING_BOAT_HUT_2(79),
        M_BUILDING_AIRSHIP_HUT_1(80),
        M_BUILDING_AIRSHIP_HUT_2(81),
        
        B_BUILDING_TEPEE(82),
        B_BUILDING_HUT(83),
        B_BUILDING_FARM(84),
        B_BUILDING_DRUM_TOWER(85),
        B_BUILDING_TEMPLE(86),
        B_BUILDING_SPY_TRAIN(87),
        B_BUILDING_WARRIOR_TRAIN(88),
        B_BUILDING_FIREWARRIOR_TRAIN(89),
        B_BUILDING_RECONVERSION(90),
        B_BUILDING_WALL_PIECE(91),
        B_BUILDING_GATE(92),
        B_BUILDING_CURR_OE_SLOT(93),
        B_BUILDING_BOAT_HUT_1(94),
        B_BUILDING_BOAT_HUT_2(95),
        B_BUILDING_AIRSHIP_HUT_1(96),
        B_BUILDING_AIRSHIP_HUT_2(97),
        
        R_BUILDING_TEPEE(98),
        R_BUILDING_HUT(99),
        R_BUILDING_FARM(100),
        R_BUILDING_DRUM_TOWER(101),
        R_BUILDING_TEMPLE(102),
        R_BUILDING_SPY_TRAIN(103),
        R_BUILDING_WARRIOR_TRAIN(104),
        R_BUILDING_FIREWARRIOR_TRAIN(105),
        R_BUILDING_RECONVERSION(106),
        R_BUILDING_WALL_PIECE(107),
        R_BUILDING_GATE(108),
        R_BUILDING_CURR_OE_SLOT(109),
        R_BUILDING_BOAT_HUT_1(110),
        R_BUILDING_BOAT_HUT_2(111),
        R_BUILDING_AIRSHIP_HUT_1(112),
        R_BUILDING_AIRSHIP_HUT_2(113),
        
        Y_BUILDING_TEPEE(114),
        Y_BUILDING_HUT(115),
        Y_BUILDING_FARM(116),
        Y_BUILDING_DRUM_TOWER(117),
        Y_BUILDING_TEMPLE(118),
        Y_BUILDING_SPY_TRAIN(119),
        Y_BUILDING_WARRIOR_TRAIN(120),
        Y_BUILDING_FIREWARRIOR_TRAIN(121),
        Y_BUILDING_RECONVERSION(122),
        Y_BUILDING_WALL_PIECE(123),
        Y_BUILDING_GATE(124),
        Y_BUILDING_CURR_OE_SLOT(125),
        Y_BUILDING_BOAT_HUT_1(126),
        Y_BUILDING_BOAT_HUT_2(127),
        Y_BUILDING_AIRSHIP_HUT_1(128),
        Y_BUILDING_AIRSHIP_HUT_2(129),
        
        G_BUILDING_TEPEE(130),
        G_BUILDING_HUT(131),
        G_BUILDING_FARM(132),
        G_BUILDING_DRUM_TOWER(133),
        G_BUILDING_TEMPLE(134),
        G_BUILDING_SPY_TRAIN(135),
        G_BUILDING_WARRIOR_TRAIN(136),
        G_BUILDING_FIREWARRIOR_TRAIN(137),
        G_BUILDING_RECONVERSION(138),
        G_BUILDING_WALL_PIECE(139),
        G_BUILDING_GATE(140),
        G_BUILDING_CURR_OE_SLOT(141),
        G_BUILDING_BOAT_HUT_1(142),
        G_BUILDING_BOAT_HUT_2(143),
        G_BUILDING_AIRSHIP_HUT_1(144),
        G_BUILDING_AIRSHIP_HUT_2(145),
        
        M_PERSON_BRAVE(146),
        M_PERSON_WARRIOR(147),
        M_PERSON_RELIGIOUS(148),
        M_PERSON_SPY(149),
        M_PERSON_FIREWARRIOR(150),
        M_PERSON_SHAMAN(151),
        
        B_PERSON_BRAVE(152),
        B_PERSON_WARRIOR(153),
        B_PERSON_RELIGIOUS(154),
        B_PERSON_SPY(155),
        B_PERSON_FIREWARRIOR(156),
        B_PERSON_SHAMAN(157),
        
        R_PERSON_BRAVE(158),
        R_PERSON_WARRIOR(159),
        R_PERSON_RELIGIOUS(160),
        R_PERSON_SPY(161),
        R_PERSON_FIREWARRIOR(162),
        R_PERSON_SHAMAN(163),
        
        Y_PERSON_BRAVE(164),
        Y_PERSON_WARRIOR(165),
        Y_PERSON_RELIGIOUS(166),
        Y_PERSON_SPY(167),
        Y_PERSON_FIREWARRIOR(168),
        Y_PERSON_SHAMAN(169),
        
        G_PERSON_BRAVE(170),
        G_PERSON_WARRIOR(171),
        G_PERSON_RELIGIOUS(172),
        G_PERSON_SPY(173),
        G_PERSON_FIREWARRIOR(174),
        G_PERSON_SHAMAN(175),
        
        BLUE_KILLED_BY_ME(176),
        RED_KILLED_BY_ME(177),
        YELLOW_KILLED_BY_ME(178),
        GREEN_KILLED_BY_ME(179),
        
        MY_NUM_KILLED_BY_BLUE(180),
        MY_NUM_KILLED_BY_RED(181),
        MY_NUM_KILLED_BY_YELLOW(182),
        MY_NUM_KILLED_BY_GREEN(183),
        
        BURN(184),
        BLAST(185),
        LIGHTNING_BOLT(186),
        WHIRLWIND(187),
        INSECT_PLAGUE(188),
        INVISIBILITY(189),
        HYPNOTISM(190),
        FIRESTORM(191),
        GHOST_ARMY(192),
        EROSION(193),
        SWAMP(194),
        LAND_BRIDGE(195),
        ANGEL_OF_DEAD(196),
        EARTHQUAKE(197),
        FLATTEN(198),
        VOLCANO(199),
        WRATH_OF_GOD(200),
        
        BRAVE(201),
        WARRIOR(202),
        RELIGIOUS(203),
        SPY(204),
        FIREWARRIOR(205),
        SHAMAN(206),
        
        TEPEE(207),
        HUT(208),
        FARM(209),
        DRUM_TOWER(210),
        TEMPLE(211),
        SPY_TRAIN(212),
        WARRIOR_TRAIN(213),
        FIREWARRIOR_TRAIN(214),
        RECONVERSION(215),
        WALL_PIECE(216),
        GATE(217),
        BOAT_HUT_1(215),
        BOAT_HUT_2(219),
        AIRSHIP_HUT_1(220),
        AIRSHIP_HUT_2(221),
        
        NO_SPECIFIC_PERSON(222),
        NO_SPECIFIC_BUILDING(223),
        NO_SPECIFIC_SPELL(224),
        
        TARGET_SHAMAN(225),
        
        M_VEHICLE_BOAT_1(226),
        M_VEHICLE_AIRSHIP_1(227),
        
        B_VEHICLE_BOAT_1(228),
        B_VEHICLE_AIRSHIP_1(229),
        
        R_VEHICLE_BOAT_1(230),
        R_VEHICLE_AIRSHIP_1(231),
        
        Y_VEHICLE_BOAT_1(232),
        Y_VEHICLE_AIRSHIP_1(233),
        
        G_VEHICLE_BOAT_1(234),
        G_VEHICLE_AIRSHIP_1(235),
        
        CP_FREE_ENTRIES(236),
        RANDOM_100(237),
        
        MUM_SHAMEN_DEFENDERS(238),
        
        CAMERA_ANGLE(239),
        CAMERA_X(240),
        CAMERA_Z(241),
        
        M_SPELL_SHIELD_COST(242),
        SHIELD(243),
        CONVERT(244),
        TELEPORT(245),
        BLOODLUST(246);
        
        /*ATTACK_MARKER(0, false),
        ATTACK_BUILDING(1, false),
        ATTACK_PERSON(2, false),
        
        ATTACK_NORMAL(0, false),
        ATTACK_BY_BOAT(1, false),
        ATTACK_BY_BALLON(2, false),
        
        GUARD_NORMAL(0, false),
        GUARD_WITH_GHOSTS(1, false);*/
        
        private final UInt16 code;
        
        private Internal(int code, boolean useOffset)
        {
            this.code = UInt16.valueOf((useOffset ? INT_OFFSET : 0) + code);
            
        }
        private Internal(int code) { this(code, true); }
        
        public final String getInternalName() { return name(); }
        
        public final UInt16 getCode() { return code; }
        
        public final boolean equalsCode(UInt16 code) { return this.code.equals(code); }
        
        private static final HashMap<UInt16, Internal> CODES = new HashMap<>();
        private static final HashMap<String, Internal> NAMES = new HashMap<>();
        static {
            for(Internal internal : values()) {
                CODES.put(internal.code, internal);
                NAMES.put(internal.name(), internal);
            }
        }
        
        public static final Internal decode(UInt16 code) 
        {
            return CODES.get(code);
        }
        
        public static final Internal decode(String name) 
        {
            return NAMES.get(name);
        }
        
        @Override
        public final String toString() { return getInternalName(); }
        
        public static final String generateOnePerLine()
        {
            Internal[] values = values();
            StringBuilder sb = new StringBuilder(values.length * 16);
            for(Internal in : values)
                sb.append(in.getInternalName()).append("\n");
            if(sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n')
                sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
        }
        
        private static String getXmlCompletions()
        {
            StringBuilder sb = new StringBuilder();
            for(Internal in : values())
            {
                sb.append("<keyword name=\"").append(in.getInternalName())
                        .append("\" type=\"constant\" returnType=\"Internal\">\n    <desc></desc>\n</keyword>\n");
            }
            return sb.toString();
        }
    }
    
    public static final void printXmlCompletions(File file)
    {
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))))
        {
            bw.write(Token.getXmlCompletions() + "\n");
            bw.write(Internal.getXmlCompletions() + "\n");
        }
        catch(IOException ex) { ex.printStackTrace(System.err); }
    }
}
