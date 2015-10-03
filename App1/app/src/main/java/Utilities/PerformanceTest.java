package Utilities;

/**
 * Created by Dale on 1/19/2015.
 */
public class PerformanceTest {
/*
    State s = null;
    public void performanceTestState(View v){
        boolean doit = true;
        if (s == null) {
            Log.e("====", "Preparing performance test");
            s = new State("a name lol", "terrain", "none");
            StateHandler.getInstance().setNewState(s);
            StateHandler.getInstance().setOldState(s);
            for (int x = 0; x < 100; x++) {
                for (int y = 0; y < 100; y++) {
                    //for each coordinate
                    //make 3 obj
                    for (int i = 0; i < 3; i++) {
                        ArrayList<Coord> al = new ArrayList<Coord>();
                        al.add(new Coord(x, y));
                        al.add(new Coord(x, y));
                        al.add(new Coord(x, y));
                        al.add(new Coord(x, y));
                        al.add(new Coord(x, y));
                        al.add(new Coord(x, y));
                        al.add(new Coord(x, y));
                        al.add(new Coord(x, y));
                        al.add(new Coord(x, y));
                        Grass g = new Grass(al, 1, 1);
                        //give each obj 5 object types
                        for (int r = 0; r < 5; r++) {
                            ArrayList<Coord> a2 = new ArrayList<Coord>();
                            a2.add(new Coord(x, y));
                            a2.add(new Coord(x, y));
                            a2.add(new Coord(x, y));
                            g.addType(new Moving(1, 1, a2, 1));
                        }

                        //add to state
                        s.addObjC(new Coord(x, y), g);
                        s.addObjID(g);
                        if(doit) {
                            doit = false;
                            try {
                                ByteArrayOutputStream bo = new ByteArrayOutputStream();
                                ObjectOutputStream os = new ObjectOutputStream(bo);
                                os.writeObject(g);
                                os.flush();
                                Log.e("Each obj grass: ", "" + bo.toByteArray().length);
                                os.close();
                                bo.close();
                            } catch (Exception e) {
                            }
                        }
                    }
                }
            }
            Log.e("====", "Done making state");
        }
        else{
            Log.e("====", "Starting performance test");
            for(int i =0; i < 100; i++){
                for(int r=0; r<100; r++){
                    ArrayList<CObj> o = StateHandler.getInstance().getNewState().getObjC(new Coord(i, r));
                    for(CObj ob : o){
                        for(ObjT ot : ob.getTypePath()){
                            String s = ot.name;
                            if(s.compareTo("Moving") ==0){
                                ob.damage(1);
                            }
                        }
                    }
                }
            }
            Log.e("====", "Done performing observing 5 ObjT from 300,000 Obj.");
        }
    }

/*    public void performanceTestMap(Activity a){
        TableLayout map = (TableLayout) a.findViewById(R.id.mapView);

        for(int i=0; i<100; i++) {
            TableRow tr = new TableRow(a);
            TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
            map.addView(tr, rowParams);
            for(int r=1; r<101; r++) {
                final Button newB = new Button(a);
                newB.setText("PrT");
                newB.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 6, a.getResources().getDisplayMetrics()));
                newB.setPadding(0,0,0,0);
                newB.setBackgroundColor(a.getResources().getColor(R.color.green_grass));
                TableRow.LayoutParams buttonParams = new TableRow.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, a.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, a.getResources().getDisplayMetrics()));
                buttonParams.column = r;
                tr.addView(newB, buttonParams);
            }

        }

    }*/
}
