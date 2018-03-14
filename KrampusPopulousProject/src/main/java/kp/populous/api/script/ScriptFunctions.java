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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import kp.populous.api.data.UInt16;
import kp.populous.api.script.ScriptConstant.Token;
import static kp.populous.api.script.ScriptConstant.Token.*;

/**
 *
 * @author Asus
 */
public final class ScriptFunctions
{
    private ScriptFunctions() {}
    
    private static final Parameter FIELD = new Parameter("Field", true);
    private static final Parameter BOOLEAN = new Parameter("State", false, Token.ON, Token.OFF);
    private static final Parameter TEAMS = new Parameter("Team", true, BLUE, RED, YELLOW, GREEN);
    
    private static final HashMap<Token, Function> FUNCTIONS = new HashMap<>();
    
    public static final Function get(Token token)
    {
        return FUNCTIONS.get(token);
    }
    
    private static void state(Token token)
    {
        Function f = new Function(token, BOOLEAN);
        FUNCTIONS.put(token, f);
    }
    private static void states(Token... tokens)
    {
        for(Token token : tokens)
            state(token);
    }
    
    private static void func(Token token, Parameter... pars)
    {
        Function f = new Function(token, pars);
        FUNCTIONS.put(token, f);
    }
    
    private static Parameter par(String type, boolean allowField, Token... values) { return new Parameter(type, allowField, values); }
    private static Parameter par(String type, Token... values) { return new Parameter(type, true, values); }
    
    
    /* State Functions */
    static {
        states(CONSTRUCT_BUILDING,
                FETCH_WOOD,
                SHAMAN_GET_WILDS,
                HOUSE_A_PERSON,
                SEND_GHOSTS,
                BRING_NEW_PEOPLE_BACK,
                TRAIN_PEOPLE,
                POPULATE_DRUM_TOWER,
                DEFEND,
                DEFEND_BASE,
                PREACH,
                BUILD_WALLS,
                SABOTAGE,
                SPELL_OFFENSIVE,
                FIREWARRIOR_DEFEND,
                BUILD_VEHICLE,
                FETCH_LOST_PEOPLE,
                FETCH_LOST_VEHICLE,
                FETCH_FAR_VEHICLE,
                AUTO_ATTACK,
                FLATTEN_BASE,
                BUILD_OUTER_DEFENCES,
                SET_AUTO_BUILD,
                SET_AUTO_HOUSE,
                DONT_HOUSE_SPECIALISTS,
                SET_REINCARNATION,
                EXTRA_WOOD_COLLECTION,
                SET_BUCKET_USAGE,
                SET_SPECIAL_NO_BLDG_PANEL,
                TURN_PUSH,
                FLYBY_ALLOW_INTERRUPT,
                GIVE_UP_AND_SULK,
                AUTO_MESSAGES);
        
        
        func(ATTACK, TEAMS, FIELD, par("AttackTarget", false, ATTACK_MARKER, ATTACK_BUILDING, ATTACK_PERSON), FIELD, FIELD, FIELD, FIELD, FIELD,
                par("AttackMode", false, ATTACK_NORMAL, ATTACK_BY_BOAT, ATTACK_BY_BALLON), FIELD, FIELD, FIELD, FIELD);
        func(SPELL_ATTACK, FIELD, FIELD, FIELD);
        func(RESET_BASE_MARKER);
        func(SET_BASE_MARKER, FIELD);
        func(SET_BASE_RADIUS, FIELD);
        func(COUNT_PEOPLE_IN_MARKER, par("TeamOrWild", true, BLUE, RED, YELLOW, GREEN, COUNT_WILD), FIELD, FIELD, FIELD);
        func(SET_DRUM_TOWER_POS, FIELD, FIELD);
        func(CONVERT_AT_MARKER, FIELD);
        func(PREACH_AT_MARKER, FIELD);
        func(SEND_GHOST_PEOPLE, FIELD);
        func(GET_SPELLS_CAST, TEAMS, FIELD, FIELD);
        func(GET_NUM_ONE_OFF_SPELLS, TEAMS, FIELD, FIELD);
        func(SET_ATTACK_VARIABLE, FIELD);
        func(BUILD_DRUM_TOWER, FIELD, FIELD);
        func(GUARD_AT_MARKER, FIELD, FIELD, FIELD, FIELD, FIELD, par("GuardType", false, GUARD_NORMAL, GUARD_WITH_GHOSTS));
        func(GUARD_BETWEEN_MARKERS, FIELD, FIELD, FIELD, FIELD, FIELD, FIELD, par("GuardType", false, GUARD_NORMAL, GUARD_WITH_GHOSTS));
        func(SPELL_DEFENSE, FIELD, FIELD, BOOLEAN);
        func(GET_HEIGHT_AT_POS, FIELD, FIELD);
        func(SEND_ALL_PEOPLE_TO_MARKER, FIELD);
        func(RESET_CONVERT_MARKER);
        func(SET_CONVERT_MARKER, FIELD);
        func(SET_MARKER_ENTRY, FIELD, FIELD, FIELD, FIELD, FIELD, FIELD, FIELD);
        func(MARKER_ENTRIES, FIELD, FIELD, FIELD, FIELD);
        func(CLEAR_GUARDING_FROM, FIELD, FIELD, FIELD, FIELD);
        func(SET_BUILDING_DIRECTION, FIELD);
        func(TRAIN_PEOPLE_NOW, FIELD, FIELD);
        func(PRAY_AT_HEAD, FIELD, FIELD);
        func(PUT_PERSON_IN_DT, FIELD, FIELD, FIELD);
        func(I_HAVE_ONE_SHOT, par("ItemType", false, SPELL_TYPE, BUILDING_TYPE), FIELD, FIELD);
        func(BOAT_PATROL, FIELD, FIELD, FIELD, FIELD, FIELD, par("VehicleType", false, BOAT_TYPE, BALLON_TYPE));
        func(DEFEND_SHAMEN, FIELD);
        func(SEND_SHAMEN_DEFENDERS_HOME);
        func(IS_BUILDING_NEAR, FIELD, FIELD, FIELD, TEAMS, FIELD, FIELD);
        func(BUILD_AT, FIELD, FIELD, FIELD, FIELD);
        func(SET_SPELL_ENTRY, FIELD, FIELD, FIELD, FIELD, FIELD, FIELD);
        func(DELAY_MAIN_DRUM_TOWER);
        func(BUILD_MAIN_DRUM_TOWER);
        func(ZOOM_TO, FIELD, FIELD, FIELD);
        func(DISABLE_USER_INPUTS);
        func(ENABLE_USER_INPUTS);
        func(OPEN_DIALOG, FIELD);
        func(GIVE_ONE_SHOT, FIELD, TEAMS);
        func(CLEAR_STANDING_PEOPLE);
        func(ONLY_STAND_AT_MARKERS);
        func(NAV_CHECK, TEAMS, par("AttackTarget", false, ATTACK_MARKER, ATTACK_BUILDING, ATTACK_PERSON), FIELD, FIELD, FIELD);
        func(TARGET_S_WARRIORS);
        func(DONT_TARGET_S_WARRIORS);
        func(TARGET_BLUE_SHAMAN);
        func(DONT_TARGET_BLUE_SHAMAN);
        func(TARGET_BLUE_DRUM_TOWERS);
        func(DONT_TARGET_BLUE_DRUM_TOWERS);
        func(HAS_BLUE_KILLED_A_GHOST, FIELD);
        func(COUNT_GUARD_FIRES, FIELD, FIELD, FIELD, FIELD);
        func(GET_HEAD_TRIGGER_COUNT, FIELD, FIELD, FIELD);
        func(MOVE_SHAMAN_TO_MARKER, FIELD);
        func(TRACK_SHAMAN_TO_ANGLE, FIELD);
        func(TRACK_SHAMAN_EXTRA_BOLLOCKS, FIELD);
        func(IS_SHAMAN_AVAILABLE_FOR_ATTACK, FIELD);
        func(PARTIAL_BUILDING_COUNT);
        func(SEND_BLUE_PEOPLE_TO_MARKER, FIELD);
        func(GIVE_MANA_TO_PLAYER, TEAMS, FIELD);
        func(IS_PLAYER_IN_WORLD_VIEW, FIELD);
        func(DESELECT_ALL_BLUE_PEOPLE);
        func(FLASH_BUTTON, FIELD, BOOLEAN);
        func(TURN_PANEL_ON, FIELD);
        func(GIVE_PLAYER_SPELL, TEAMS, FIELD);
        func(HAS_PLAYER_BEEN_IN_ENCYC, FIELD);
        func(IS_BLUE_SHAMAN_SELECTED, FIELD);
        func(CLEAR_SHAMAN_LEFT_CLICK);
        func(CLEAR_SHAMAN_RIGHT_CLICK);
        func(IS_SHAMAN_ICON_LEFT_CLICKED, FIELD);
        func(IS_SHAMAN_ICON_RIGHT_CLICKED, FIELD);
        func(TRIGGER_THING, FIELD);
        func(TRACK_TO_MARKER, FIELD);
        func(CAMERA_ROTATION, FIELD);
        func(STOP_CAMERA_ROTATION);
        func(COUNT_BLUE_SHAPES, FIELD);
        func(COUNT_BLUE_IN_HOUSES, FIELD);
        func(HAS_HOUSE_INFO_BEEN_SHOWN, FIELD);
        func(CLEAR_HOUSE_INFO_FLAG);
        func(COUNT_BLUE_WITH_BUILD_COMMAND, FIELD);
        func(TARGET_PLAYER_DT_AND_S, TEAMS);
        func(REMOVE_PLAYER_THING, TEAMS, FIELD);
        func(SET_WOOD_COLLECTION_RADII, FIELD, FIELD, FIELD, FIELD);
        func(GET_NUM_PEOPLE_CONVERTED, TEAMS, FIELD);
        func(GET_NUM_PEOPLE_BEING_PREACHED, TEAMS, FIELD);
        func(TRIGGER_LEVEL_LOST);
        func(TRIGGER_LEVEL_WON);
        func(REMOVE_HEAD_AT_POS, FIELD, FIELD);
        func(SET_BUCKET_COUNT_FOR_SPELL, FIELD, FIELD);
        func(CREATE_MSG_NARRATIVE, FIELD);
        func(CREATE_MSG_OBJECTIVE, FIELD);
        func(CREATE_MSG_INFORMATION, FIELD);
        func(CREATE_MSG_INFORMATION_ZOON, FIELD, FIELD, FIELD, FIELD);
        func(SET_MSG_ZOON, FIELD, FIELD, FIELD);
        func(SET_MSG_TIMEOUT, FIELD);
        func(SET_MSG_DELETE_ON_OK);
        func(SET_MSG_RETURN_ON_OK);
        func(SET_MSG_DELETE_ON_RMB_ZOOM);
        func(SET_MSG_OPEN_DLG_ON_RMB_ZOOM);
        func(SET_MSG_CREATE_RETURN_MSG_ON_RMB_ZOOM);
        func(SET_MSG_OPEN_DLG_ON_RMB_DELETE);
        func(SET_MSG_ZOOM_ON_LMB_OPEN_DLG);
        func(SET_MSG_AUTO_OPEN_DLG);
        func(SET_MSG_OK_SAVE_EXIT_DLG);
        func(FIX_WILD_IN_AREA, FIELD, FIELD, FIELD);
        func(CHECK_IF_PERSON_PREACHED_TO, FIELD, FIELD, FIELD);
        func(COUNT_ANGELS, TEAMS, FIELD);
        func(SET_NO_BLUE_REINC);
        func(IS_SHAMAN_IN_AREA, TEAMS, FIELD, FIELD, FIELD);
        func(FORCE_TOOLTIP, FIELD, FIELD, FIELD, FIELD);
        func(SET_DEFENCE_RADIUS, FIELD);
        func(MARVELLOUS_HOUSE_DEATH);
        func(CALL_TO_ARMS);
        func(DELETE_SMOKE_STUFF, FIELD, FIELD, FIELD);
        func(SET_TIMER_GOING, FIELD);
        func(REMOVE_TIMER);
        func(HAS_TIMER_REACHED_ZERO, FIELD);
        func(START_REINC_NOW);
        func(FLYBY_CREATE_NEW);
        func(FLYBY_START);
        func(FLYBY_STOP);
        func(FLYBY_SET_EVENT_POS, FIELD, FIELD, FIELD, FIELD);
        func(FLYBY_SET_EVENT_ANGLE, FIELD, FIELD, FIELD);
        func(FLYBY_SET_EVENT_ZOOM, FIELD, FIELD, FIELD);
        func(FLYBY_SET_EVENT_INT_POINT, FIELD, FIELD, FIELD, FIELD);
        func(FLYBY_SET_EVENT_TOOLTIP, FIELD, FIELD, FIELD, FIELD, FIELD);
        func(FLYBY_SET_END_TARGET, FIELD, FIELD, FIELD, FIELD);
        func(FLYBY_SET_MESSAGE, FIELD, FIELD);
        func(KILL_TEAM_IN_AREA, FIELD, FIELD, FIELD);
        func(CLEAR_ALL_MSG);
        func(SET_MSG_ID, FIELD);
        func(GET_MSG_ID, FIELD);
        func(KILL_ALL_MSG_ID, FIELD);
        func(IS_PRISION_ON_LEVEL, FIELD);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public static final class Parameter
    {
        private final String type;
        private final HashSet<Token> tokens;
        private final boolean field;
        
        private Parameter(String type, boolean allowField) { this.type = type; tokens = null; field = allowField; }
        
        private Parameter(String type, boolean allowField, Token... tokens) {
            this.type = type;
            this.tokens = new HashSet<>(Arrays.asList(tokens));
            field = allowField;
        }
        
        public final boolean allowField() { return field; }
        public final boolean isValidToken(Token token) { return tokens != null && tokens.contains(token); }
        public final boolean isValidToken(UInt16 value)
        {
            Token token = Token.decode(value);
            return token != null && isValidToken(token);
        }
        public final boolean isValidToken(String name)
        {
            Token token = Token.decode(name);
            return token != null && isValidToken(token);
        }
        
        public final String getTokensStringList()
        {
            if(tokens == null)
                return "[]";
            return tokens.toString();
        }
        
        private String possibleValues()
        {
            if(tokens == null)
                return field ? "[Variable, Constant, Internal]" : "[]";
            if(!field)
                return tokens.toString();
            List<String> list = tokens.stream().map(t -> t.toString()).collect(Collectors.toList());
            list.add("Variable");
            list.add("Constant");
            list.add("Internal");
            return list.toString();
        }
        
        public final String generateCompletionXml()
        {
            String name = Character.toLowerCase(type.charAt(0)) + type.substring(1);
            return PARAM_TAB + "<param name=\"" + name + "\">\n" +
                    PARAM_DESC_TAB + "<desc>" + possibleValues() + "</desc>\n" +
                    PARAM_TAB + "</param>";
        }
    }
    
    
    public static final class Function
    {
        private final Token command;
        private final Parameter[] pars;
        
        private Function(Token token, Parameter... pars)
        {
            if(token == null)
                throw new NullPointerException();
            if(!token.isFunction())
                throw new IllegalStateException();
            if(pars == null)
                throw new NullPointerException();
            this.command = token;
            this.pars = pars;
        }
        
        public final Token getCommandToken() { return command; }
        public final int getParameterCount() { return pars.length; }
        public final Parameter getParameter(int index) { return pars[index]; }
        
        private String xmlParams()
        {
            if(pars.length < 1)
                return "";
            StringBuilder sb = new StringBuilder();
            for(int i=0;i<pars.length;i++)
            {
                sb.append(pars[i].generateCompletionXml());
                if(i + 1 < pars.length)
                    sb.append('\n');
            }
            return sb.toString();
        }
        
        public final String generateCompletionXml()
        {
            return FUNC_TAB + "<keyword name=\"" + command.getFunctionName() + "\" type=\"function\">\n" +
                    PARAMS_TAB + "<params>\n" + xmlParams() + "\n" + PARAMS_TAB + "</params>\n" +
                    DESC_TAB + "<desc></desc>\n" + FUNC_TAB + "</keyword>";
        }
    }
    
    public static final void printXmlCompletions(File file)
    {
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))))
        {
            for(Function func : FUNCTIONS.values())
                bw.write(func.generateCompletionXml() + "\n");
        }
        catch(IOException ex) { ex.printStackTrace(System.err); }
    }
    
    private static final String TAB = "    ";
    private static final String FUNC_TAB = "";
    private static final String PARAMS_TAB = TAB;
    private static final String DESC_TAB = TAB;
    private static final String PARAM_TAB = TAB + TAB;
    private static final String PARAM_DESC_TAB = TAB + TAB + TAB;
}
