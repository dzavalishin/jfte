package ru.dz.jfte;

public class EFrame extends GFrame implements GuiDefs, ModeDefs 
{
    EEventMap CMap = null;
    EModel CModel = null;

    
    
    EFrame(int XSize, int YSize) {
    	super(XSize, YSize);
    	GUI.frames = this;
    }


    void Update() {
        GxView V = (GxView)Active;

        if (V!=null) {
            if (CModel != EModel.ActiveModel[0] && EModel.ActiveModel[0]!=null) {
                String[] Title = {""}; //fte: ";
                String[] STitle = {""}; //"fte: ";

                EModel.ActiveModel[0].GetTitle(Title,STitle);
                ConSetTitle(Title[0], STitle[0]);
                CModel = EModel.ActiveModel[0];
            }
        }
        super.Update();
    }

    void UpdateMenu() {
        GxView V = (GxView )Active;
        EEventMap Map = null;

        if (V!=null)
            Map = V.GetEventMap();

        if (Map != CMap || CMap == null) {
        	String Menu = null;
            String OMenu = null;
            // set menu

            if (CMap!=null)
                OMenu = CMap.GetMenu(EM_MainMenu);
            if (Map!=null)
                Menu = Map.GetMenu(EM_MainMenu);
            if (Menu == null)
                Menu = "Main";
            CMap = Map;

            if (OMenu!=null && OMenu.equals(Menu)) {
                // ok
            } else {
                SetMenu(Menu);
            }
        } /*else if (CMap == 0 && Map == 0) {
            SetMenu("Main");
        }*/

        super.UpdateMenu();
    }
    
}
