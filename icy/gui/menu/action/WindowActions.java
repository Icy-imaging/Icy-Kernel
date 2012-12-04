/**
 * 
 */
package icy.gui.menu.action;

import icy.common.IcyAbstractAction;
import icy.gui.main.MainFrame;
import icy.main.Icy;
import icy.preferences.GeneralPreferences;
import icy.resource.ResourceUtil;
import icy.resource.icon.IcyIcon;
import icy.swimmingPool.SwimmingPoolViewer;

import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Stephane
 */
public class WindowActions
{
    public static IcyAbstractAction stayOnTopAction = new IcyAbstractAction("Stay on top", new IcyIcon(
            ResourceUtil.ICON_PIN), "Keep window on top", "Icy window always stays above other windows.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 389778521530821291L;

        @Override
        public void doAction(ActionEvent e)
        {
            final boolean value = !Icy.getMainInterface().isAlwaysOnTop();

            // set "always on top" state
            Icy.getMainInterface().setAlwaysOnTop(value);
            // and save state
            GeneralPreferences.setAlwaysOnTop(value);
        }
    };

    public static IcyAbstractAction swimmingPoolAction = new IcyAbstractAction("Swimming Pool Viewer", new IcyIcon(
            ResourceUtil.ICON_DATABASE), "Show the swimming pool objects")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -2243906270795266643L;

        @Override
        public void doAction(ActionEvent e)
        {
            new SwimmingPoolViewer();
        }
    };

    public static IcyAbstractAction gridTileAction = new IcyAbstractAction("Grid", new IcyIcon("2x2_grid"),
            "Grid tile arrangement", "Reorganise all opened windows in grid tile.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 5529845883985655784L;

        @Override
        public void doAction(ActionEvent e)
        {
            final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
            if (mainFrame != null)
                mainFrame.organizeTile(MainFrame.TILE_GRID);
        }
    };

    public static IcyAbstractAction horizontalTileAction = new IcyAbstractAction("Horizontal tile", new IcyIcon(
            "tile_horizontal"), "Horizontal tile arrangement", "Reorganise all opened windows in horizontal tile.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 5752682613042198566L;

        @Override
        public void doAction(ActionEvent e)
        {
            final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
            if (mainFrame != null)
                mainFrame.organizeTile(MainFrame.TILE_HORIZONTAL);
        }
    };

    public static IcyAbstractAction verticalTileAction = new IcyAbstractAction("Vertical tile", new IcyIcon(
            "tile_vertical"), "Vertical tile arrangement", "Reorganise all opened windows in vertical tile.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = -3978957277869827951L;

        @Override
        public void doAction(ActionEvent e)
        {
            final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
            if (mainFrame != null)
                mainFrame.organizeTile(MainFrame.TILE_VERTICAL);
        }
    };

    public static IcyAbstractAction cascadeAction = new IcyAbstractAction("Cascade", new IcyIcon("cascade"),
            "Cascade arrangement", "Reorganise all opened windows in cascade.")
    {
        /**
         * 
         */
        private static final long serialVersionUID = 5074922972421168033L;

        @Override
        public void doAction(ActionEvent e)
        {
            final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
            if (mainFrame != null)
                mainFrame.organizeCascade();
        }
    };

    /**
     * Return all actions of this class
     */
    public static List<IcyAbstractAction> getAllActions()
    {
        final List<IcyAbstractAction> result = new ArrayList<IcyAbstractAction>();

        for (Field field : WindowActions.class.getFields())
        {
            final Class<?> type = field.getType();

            try
            {
                if (type.isAssignableFrom(IcyAbstractAction[].class))
                    result.addAll(Arrays.asList(((IcyAbstractAction[]) field.get(null))));
                else if (type.isAssignableFrom(IcyAbstractAction.class))
                    result.add((IcyAbstractAction) field.get(null));
            }
            catch (Exception e)
            {
                // ignore
            }
        }

        return result;
    }
}
