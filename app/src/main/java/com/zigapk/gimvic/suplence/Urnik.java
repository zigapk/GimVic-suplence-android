package com.zigapk.gimvic.suplence;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by ziga on 10/21/14.
 */
public class Urnik {

    public String[][] urnik;

    public static void downloadUrnik(Context context){

        String rawData = Internet.getTextFromUrl("http://app.gimvic.org/APIv2/urnik/urnik_provider.php?hash=" + Settings.getUrnikHash(context));

        if(!rawData.contains("no_new_data")) {
            if(rawData.contains("podatki = new Array(")){
                Files.writeToFile("Urnik.js", rawData, context);
                Settings.setUrnikDownloaded(true, context);
                Settings.setUrnikHash(Internet.getTextFromUrl("http://app.gimvic.org/APIv2/urnik/urnik_hash_provider.php"), context);

                Settings.setUrnikParsed(false, context);
                Settings.setTrueUrnikParsed(false, context);
                Urnik.parseUrnik(context);
            }
        }

        Settings.setUrnikDownloaded(true, context);
    }

    public static void render(Context context){
        while (!Other.layoutComponentsReady() || !Settings.isTrueUrnikParsed(context)){}

        Gson gson = new Gson();
        PersonalUrnik urnik = gson.fromJson(Files.getFileValue("Urnik-personal.json", context), PersonalUrnik.class);

        renderPersonalUrnik(urnik, context);

    }

    public static void renderPersonalUrnik(PersonalUrnik urnik, final Context context){

        final PersonalUrnik originalUrnik = getPersonalUrnik(context);

        while (!Other.layoutComponentsReady()){}
        for(int dan = 1; dan <= 5; dan++){
            int emptyCounter = 0;

            for(int ura = 1; ura <= 9; ura++){
                final UrnikElement current = urnik.days[dan - 1].classes[ura - 1];

                if(current.empty){
                    while (Main.classItems[dan - 1][ura - 1] == null){}
                    final LinearLayout currentClass = Main.classItems[dan - 1][ura - 1];
                    //run on ui thread
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            currentClass.setVisibility(View.GONE);
                        }
                    });
                    emptyCounter++;

                }else {
                    final LinearLayout currentClass = Main.classItems[dan - 1][ura - 1];

                    final Context tempContext = context;
                    //run on ui thread
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            currentClass.setVisibility(View.VISIBLE);
                            if(current.suplenca) {
                                currentClass.setBackground(tempContext.getResources().getDrawable(R.drawable.bg_card_green));
                            } else {
                                currentClass.setBackground(tempContext.getResources().getDrawable(R.drawable.bg_card));
                            }
                        }
                    });

                    while (!Other.layoutComponentsReady()) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    final TextView predmetTv = Main.textViews[dan - 1][ura - 1][0];
                    final TextView profesorTv = Main.textViews[dan - 1][ura - 1][1];
                    final TextView ucilnicaTv = Main.textViews[dan - 1][ura - 1][2];
                    final TextView opomba = Main.textViews[dan - 1][ura - 1][3];
                    final int tempDan = dan;
                    final int tempUra = ura;


                    if(Settings.getUserMode(context) == UserMode.MODE_UCITELJ){

                        //run on ui thread
                        Handler handler2 = new Handler(Looper.getMainLooper());
                        handler2.post(new Runnable() {
                            public void run() {
                                predmetTv.setText(current.predmetToString());
                                if (current.predmetToString().length() >= 7)
                                    predmetTv.setTextSize(22);
                                else predmetTv.setTextSize(28);

                                if(current.mankajociUcitelj){
                                    profesorTv.setText(current.profToString());
                                    if (current.profToString().contains("/"))
                                        profesorTv.setTextSize(13);
                                    else profesorTv.setTextSize(16);
                                }else {
                                    profesorTv.setText(current.razredToString());
                                    if (current.razredToString().contains("/"))
                                        profesorTv.setTextSize(13);
                                    else profesorTv.setTextSize(16);
                                }

                                ucilnicaTv.setText(current.ucilnicaToString());
                                if (current.ucilnicaToString().length() > 4)
                                    ucilnicaTv.setTextSize(22);
                                else ucilnicaTv.setTextSize(28);

                                if(current.opomba != "" || current.opomba == null){
                                    opomba.setVisibility(View.VISIBLE);
                                    opomba.setText(context.getResources().getString(R.string.opomba) + " " + current.opomba);
                                }else {
                                    opomba.setVisibility(View.GONE);
                                    opomba.setText("");
                                }

                                if (current.suplenca){
                                    final UrnikElement temp = originalUrnik.days[tempDan - 1].classes[tempUra - 1];
                                    if (!current.predmeti.equals(temp.predmeti))
                                        predmetTv.setTextColor(context.getResources().getColor(R.color.white));
                                    else predmetTv.setTextColor(context.getResources().getColor(R.color.black));
                                    if(current.mankajociUcitelj){
                                        profesorTv.setTextColor(context.getResources().getColor(R.color.white));
                                    }else {
                                        profesorTv.setTextColor(context.getResources().getColor(R.color.white));
                                    }
                                    if (!current.ucilnice.equals(temp.ucilnice))
                                        ucilnicaTv.setTextColor(context.getResources().getColor(R.color.white));
                                    else ucilnicaTv.setTextColor(context.getResources().getColor(R.color.black));
                                }else {
                                    predmetTv.setTextColor(context.getResources().getColor(R.color.black));
                                    ucilnicaTv.setTextColor(context.getResources().getColor(R.color.black));
                                    profesorTv.setTextColor(context.getResources().getColor(R.color.black));
                                }
                            }
                        });

                    }else{
                        //run on ui thread
                        Handler handler2 = new Handler(Looper.getMainLooper());
                        handler2.post(new Runnable() {
                            public void run() {
                                predmetTv.setText(current.predmetToString());
                                if (current.predmetToString().length() >= 7)
                                    predmetTv.setTextSize(22);
                                else predmetTv.setTextSize(28);

                                profesorTv.setText(current.profToString());
                                if (current.profToString().contains("/"))
                                    profesorTv.setTextSize(13);
                                else profesorTv.setTextSize(16);

                                ucilnicaTv.setText(current.ucilnicaToString());
                                if (current.ucilnicaToString().length() > 4)
                                    ucilnicaTv.setTextSize(22);
                                else ucilnicaTv.setTextSize(28);

                                if(current.opomba != "" || current.opomba == null){
                                    opomba.setVisibility(View.VISIBLE);
                                    opomba.setText(context.getResources().getString(R.string.opomba) + " " + current.opomba);
                                }else {
                                    opomba.setVisibility(View.GONE);
                                    opomba.setText("");
                                }

                                if (current.suplenca){
                                    final UrnikElement temp = originalUrnik.days[tempDan - 1].classes[tempUra - 1];
                                    if (!current.predmeti.equals(temp.predmeti))
                                        predmetTv.setTextColor(context.getResources().getColor(R.color.white));
                                    else predmetTv.setTextColor(context.getResources().getColor(R.color.black));
                                    if (!current.ucilnice.equals(temp.ucilnice))
                                        ucilnicaTv.setTextColor(context.getResources().getColor(R.color.white));
                                    else ucilnicaTv.setTextColor(context.getResources().getColor(R.color.black));
                                    if (!current.profesorji.equals(temp.profesorji))
                                        profesorTv.setTextColor(context.getResources().getColor(R.color.white));
                                    else profesorTv.setTextColor(context.getResources().getColor(R.color.black));
                                }else {
                                    predmetTv.setTextColor(context.getResources().getColor(R.color.black));
                                    ucilnicaTv.setTextColor(context.getResources().getColor(R.color.black));
                                    profesorTv.setTextColor(context.getResources().getColor(R.color.black));
                                }
                            }
                        });

                    }


                }
            }

            if(emptyCounter == 9){
                final ImageView checkmark = Main.checkmarks[dan - 1];
                //run on ui thread
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        checkmark.setVisibility(View.VISIBLE);
                    }
                });
            }else {
                final ImageView checkmark = Main.checkmarks[dan - 1];
                //run on ui thread
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        checkmark.setVisibility(View.GONE);
                    }
                });
            }

        }

    }

    public static void parseUrnik(final Context context){

        new Thread() {
            @Override
            public void run() {
                Settings.setTrueUrnikParsed(false, context);
                while (!Settings.isUrnikDownloaded(context)){
                    try {
                        Thread.sleep(30);
                    }catch (Exception e){}
                }

                String rawData = Files.getFileValue("Urnik.js", context);

                Urnik urnik = new Urnik();
                urnik.urnik = parseUrnikFromString(rawData);

                Gson gson = new Gson();
                Files.writeToFile("Urnik.json", gson.toJson(urnik), context);

                parsePersonalUrnik(context);
                Settings.setTrueUrnikParsed(true, context);
            }
        }.start();
    }

    private static String[][] parseUrnikFromString(String rawData){
        String array[] = rawData.split("podatki");
        ArrayList<String> dataArray = new ArrayList<String>();
        for (int i = 2; i < array.length; i++)
            dataArray.add(array[i]);

        String last = dataArray.get(dataArray.size()-1);
        String lastCleared = "";
        for (int i = 0; i < last.length(); i++) {
            if (!last.substring(i, i+1).contains("r")) lastCleared += last.substring(i, i+1);
            else break;
        }
        dataArray.set(dataArray.size() - 1, lastCleared);

        for (int i = 0; i < dataArray.size(); i++) {
            String string = dataArray.get(i);
            if (string.contains("new Array")) {
                dataArray.remove(i);
                i--;
            }
            else {
                string = string.replaceAll("]\\[", ";");
                string = string.replaceAll("\\[", "");
                string = string.replaceAll("]", "");
                string = string.replaceAll("\"", "");
                string = string.replace(" = ", ";");
                dataArray.set(i, string);
            }
        }

        int lastIndex1;
        String string = "";
        String lastString = dataArray.get(dataArray.size() - 1);
        for (int i = 0; i < lastString.length(); i++) {
            if (lastString.substring(i, i+1).contains(";"))
                break;
            string += lastString.substring(i, i+1);
        }

        lastIndex1 = Integer.parseInt(string);
        boolean start = false;
        for (int i = 0; i < lastString.length(); i++) {
            if (start) {
                string = lastString.substring(i, i + 1);
                break;
            }
            if (lastString.substring(i, i+1).contains(";"))
                start = true;
        }
        int lastIndex2 = Integer.parseInt(string);

        String finalData[][] = new String[lastIndex1+1][lastIndex2+1];
        for (int i = 0; i < dataArray.size(); i++) {
            String components[] = dataArray.get(i).split(";");
            if (components.length == 3)
                finalData[Integer.parseInt(components[0])][Integer.parseInt(components[1])] = components[2];
            else
                finalData[Integer.parseInt(components[0])][Integer.parseInt(components[1])] = "";
        }
        return finalData;
    }
    public static Razredi parseRazredi(Context context){

        String rawData = Files.getFileValue("Urnik.js", context);

        String razrediData = rawData.substring(rawData.indexOf("razredi = new Array("), rawData.indexOf("ucitelji = new Array("));

        Razredi result = new Razredi();

        String temp = "razredi = new Array(";
        int start = razrediData.indexOf(temp) + temp.length();
        int stop = razrediData.indexOf(");", start);

        int number = Integer.parseInt(razrediData.substring(start, stop));

        result.razredi = new String[number];

        for(int i = 0; i < number; i++){
            String before = "razredi[" + i + "] = \"";
            String after = "\";";

            int zacetek = razrediData.indexOf(before) + before.length();
            int konec = razrediData.indexOf(after, zacetek);

            result.razredi[i] = razrediData.substring(zacetek, konec);
        }

        return result;
    }

    public static Ucitelji parseUcitelji(Context context){


        String rawData = Files.getFileValue("Urnik.js", context);
        String uciteljiData = rawData.substring(rawData.indexOf("ucitelji = new Array("), rawData.indexOf("ucilnice = new Array("));

        Ucitelji result = new Ucitelji();

        String temp = "ucitelji = new Array(";
        int start = uciteljiData.indexOf(temp) + temp.length();
        int stop = uciteljiData.indexOf(");", start);

        int number = Integer.parseInt(uciteljiData.substring(start, stop));

        result.ucitelji = new String[number];

        for(int i = 0; i < number; i++){
            String before = "ucitelji[" + i + "] = \"";
            String after = "\";";

            int zacetek = uciteljiData.indexOf(before) + before.length();
            int konec = uciteljiData.indexOf(after, zacetek);

            result.ucitelji[i] = uciteljiData.substring(zacetek, konec);
        }

        return result;
    }

    public static void parsePersonalUrnik(Context context){

        while (Settings.getRazredi(context).razredi.size() == 0 && Settings.getProfesor(context) == ""){}

        Gson gson = new Gson();
        Urnik urnik = gson.fromJson(Files.getFileValue("Urnik.json", context), Urnik.class);

        PersonalUrnik personal = new PersonalUrnik();


        int mode = Settings.getUserMode(context);

        if(mode == UserMode.MODE_UCENEC){

            ChosenRazredi razredi = Settings.getRazredi(context);
            for(int i = 0; i < urnik.urnik.length; i++){
                if(Other.razredEqualsAny(urnik.urnik[i][1], razredi)){
                    int ura = Integer.parseInt(urnik.urnik[i][6]);
                    int dan = Integer.parseInt(urnik.urnik[i][5]);
                    String profesor = urnik.urnik[i][2];
                    String predmet = urnik.urnik[i][3];
                    String ucilnica = urnik.urnik[i][4];

                    personal.days[dan - 1].classes[ura - 1].razredi.add(urnik.urnik[i][1]);
                    personal.days[dan - 1].classes[ura - 1].ura = ura;
                    personal.days[dan - 1].classes[ura - 1].dan = dan;
                    personal.days[dan - 1].classes[ura - 1].profesorji.add(profesor);
                    personal.days[dan - 1].classes[ura - 1].predmeti.add(predmet);
                    personal.days[dan - 1].classes[ura - 1].ucilnice.add(ucilnica);
                    personal.days[dan - 1].classes[ura - 1].empty = false;

                }
            }

        }else if(mode == UserMode.MODE_UCITELJ){

            String profesor = Settings.getProfesor(context);
            for(int i = 0; i < urnik.urnik.length; i++){
                if(urnik.urnik[i][2].equals(profesor)){
                    int ura = Integer.parseInt(urnik.urnik[i][6]);
                    int dan = Integer.parseInt(urnik.urnik[i][5]);
                    String razred = urnik.urnik[i][1];
                    String predmet = urnik.urnik[i][3];
                    String ucilnica = urnik.urnik[i][4];

                    personal.days[dan - 1].classes[ura - 1].razredi.add(razred);
                    personal.days[dan - 1].classes[ura - 1].ura = ura;
                    personal.days[dan - 1].classes[ura - 1].dan = dan;
                    personal.days[dan - 1].classes[ura - 1].profesorji.add(profesor);
                    personal.days[dan - 1].classes[ura - 1].predmeti.add(predmet);
                    personal.days[dan - 1].classes[ura - 1].ucilnice.add(ucilnica);
                    personal.days[dan - 1].classes[ura - 1].empty = false;

                }
            }
        }

        String json = gson.toJson(personal);
        Files.writeToFile("Urnik-personal.json", json, context);

        Settings.setUrnikParsed(true, context);
        Settings.setTrueUrnikParsed(true, context);

    }

    public static PersonalUrnik getPersonalUrnik(Context context){
        String file = Files.getFileValue("Urnik-personal.json", context);
        while (file == "" || file == null){
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            file = Files.getFileValue("Urnik-personal.json", context);
        }
        PersonalUrnik temp = new Gson().fromJson(file, PersonalUrnik.class);
        return temp;
    }

}

class Razredi{
    String[] razredi;
}

class Ucitelji{
    String[] ucitelji;
}

class PersonalUrnik{
    Day[] days;

    public PersonalUrnik(){
        days = new Day[5];
        days[0] = new Day();
        days[1] = new Day();
        days[2] = new Day();
        days[3] = new Day();
        days[4] = new Day();
    }
}

class UrnikElement{
    ArrayList<String> razredi = new ArrayList<>();
    ArrayList<String> profesorji = new ArrayList<>();
    ArrayList<String> predmeti = new ArrayList<>();
    ArrayList<String> ucilnice = new ArrayList<>();
    String opomba = "";
    int ura = 1;
    int dan = 1; // 1 = monday, 5 = friday
    boolean suplenca = false;
    boolean mankajociUcitelj = false;
    boolean empty = true;

    private static String everyToString(ArrayList<String> data) {
        ArrayList<String> added = new ArrayList<>();
        String result = "";
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) != null && data.get(i) != "") {
                if (!isAllreadyAdded(data.get(i), added)) {
                    if (i != 0) result += "/";
                    result += data.get(i);
                    added.add(data.get(i));
                }
            }
        }
        if (result == "") return "/";
        return result;
    }

    private static boolean isAllreadyAdded(String str, ArrayList<String> done) {
        for (String doneElement : done) {
            if (str.equals(doneElement)) return true;
        }
        return false;
    }

    public String razredToString() {
        return everyToString(razredi);
    }

    public String profToString() {
        return everyToString(profesorji);
    }

    public String predmetToString() {
        return everyToString(predmeti);
    }

    public String ucilnicaToString() {
        return everyToString(ucilnice);
    }
}

class Day{
    UrnikElement[] classes;

    public Day(){
        classes = new UrnikElement[9];
        classes[0] = new UrnikElement();
        classes[1] = new UrnikElement();
        classes[2] = new UrnikElement();
        classes[3] = new UrnikElement();
        classes[4] = new UrnikElement();
        classes[5] = new UrnikElement();
        classes[6] = new UrnikElement();
        classes[7] = new UrnikElement();
        classes[8] = new UrnikElement();
    }
}