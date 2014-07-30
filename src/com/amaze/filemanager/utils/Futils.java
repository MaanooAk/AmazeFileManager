package com.amaze.filemanager.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amaze.filemanager.R;
import com.amaze.filemanager.activities.MainActivity;
import com.amaze.filemanager.activities.TextReader;
import com.amaze.filemanager.adapters.DialogAdapter;
import com.amaze.filemanager.fragments.Main;
import com.amaze.filemanager.services.DeleteTask;
import com.amaze.filemanager.services.ExtractService;
import com.amaze.filemanager.services.ZipTask;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.Command;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class Futils {
    public Futils() {
    }

    ArrayList<File> lis = new ArrayList<File>();
    ArrayList<File> images = new ArrayList<File>();
    AlertDialog.Builder b = null;

    public void scanFile(String path, Context c) {

        Uri contentUri = Uri.fromFile(new File(path));
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
        c.sendBroadcast(mediaScanIntent);
    }

    public String getString(Context c, int a) {
        return c.getResources().getString(a);
    }

    public String readableFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size
                / Math.pow(1024, digitGroups))
                + "" + units[digitGroups];
    }

    public String getFileExtension(String url) {
        try {
            int a = url.lastIndexOf(".");
            int b = url.length();
            return url.substring(a, b);
        } catch (Exception e) {
            return "";
        }

    }


    public void openunknown(File f, Context c) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);

        String type = MimeTypes.getMimeType(f);
        intent.setDataAndType(Uri.fromFile(f), type);
        c.startActivity(intent);

    }

    public void openWith(File f, Context c) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(f), "*/*");
        c.startActivity(intent);

    }

    public String getSize(File f) {
        long i = f.length();

        return readableFileSize(i);

    }

    public void deleteFiles(ArrayList<Layoutelements> a, final Main b, List<Integer> pos) {
        AlertDialog.Builder c = new AlertDialog.Builder(b.getActivity());
        View v = b.getActivity().getLayoutInflater().inflate(R.layout.dialoginfo, null);
        TextView tb = (TextView) v.findViewById(R.id.info);
        c.setTitle(getString(b.getActivity(), R.string.confirm));
        String names = "";
        final ArrayList<File> todelete = new ArrayList<File>();
        for (int i = 0; i < pos.size(); i++) {
            String path = a.get(pos.get(i)).getDesc();
            todelete.add(new File(path));
            names = names + "\n" + "(" + (i + 1) + ".)" + new File(path).getName();
        }
        tb.setText(getString(b.getActivity(), R.string.questiondelete) + names);
        c.setView(v);
        c.setNegativeButton(getString(b.getActivity(), R.string.no), null);
        c.setPositiveButton(getString(b.getActivity(), R.string.yes), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface p1, int p2) {
                Crouton.makeText(b.getActivity(), getString(b.getActivity(), R.string.deleting), Style.INFO).show();
                if(todelete.get(0).getParentFile().canWrite()){
                Intent i = new Intent(b.getActivity(), DeleteTask.class);
                i.putStringArrayListExtra("files", toStringArray(todelete));
                b.getActivity().startService(i);}
                else if(b.rootMode){for(File f:todelete){
                    RootTools.deleteFileOrDirectory(f.getPath(),true);}
                    b.updateList();
                }
            }
        });
        c.show();
    }

    public String count(File f) {
        try {
            return (f.listFiles().length + " items");
        } catch (Exception e) {
            return "";
        }
    }

    public boolean canGoBack(File f) {
        try {
            f.getParentFile().listFiles();
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }


    public String getdate(File f) {

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy | KK:mm a");
        return (sdf.format(f.lastModified())).toString();
    }

    public ArrayList<File> getImages(File f) {

        getImage(f);

        return images;
    }

    public void getImage(File file) {

        if (file.isDirectory()) {
            if (!file.getName().equals(".thumbnails")) {
                // do you have permission to read this directory?
                if (file.canRead()) {
                    for (File temp : file.listFiles()) {
                        if (temp.isDirectory()) {

                            getImage(temp);

                        } else {
                            if (getFileExtension(temp.getName()).equals(".jpg")
                                    || getFileExtension(temp.getName()).equals(
                                    ".jpeg")
                                    || getFileExtension(temp.getName()).equals(
                                    (".png"))) {
                                images.add(temp);
                            }
                        }
                    }
                } else {
                    System.out
                            .println(file.getAbsoluteFile() + "Permission Denied");
                }
            }
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options,
                                     int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public void showProps(File f, Activity c) {
        String date = getString(c, R.string.date) + getdate(f);
        String items = "", size = "", name, parent;
        name = getString(c, R.string.name) + f.getName();
        parent = getString(c, R.string.location) + f.getParent();
        if (f.isDirectory()) {
            size = getString(c, R.string.size) + readableFileSize(folderSize(f));
            items = getString(c, R.string.totalitems) + count(f);
        } else if (f.isFile()) {
            items = "";
            size = getString(c, R.string.size) + getSize(f);
        }
        AlertDialog.Builder a = new AlertDialog.Builder(c);
        View v = c.getLayoutInflater().inflate(R.layout.dialoginfo, null);
        TextView tb = (TextView) v.findViewById(R.id.info);
        a.setTitle(getString(c, R.string.properties));
        tb.setText(name + "\n" + parent + "\n" + size + "\n" + items + "\n"
                + date);
        a.setView(v);
        a.show();
    }

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    public Bundle getPaths(String path, Context c) {
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> paths = new ArrayList<String>();
        Bundle b = new Bundle();
        while (path.contains("/")) {

            paths.add(path);
            names.add(path.substring(1 + path.lastIndexOf("/"), path.length()));
            path = path.substring(0, path.lastIndexOf("/"));
        }
        names.remove("");
        paths.remove("/");
        names.add("root");
        paths.add("/");
        // Toast.makeText(c,paths.get(0)+"\n"+paths.get(1)+"\n"+paths.get(2),Toast.LENGTH_LONG).show();
        b.putStringArrayList("names", names);
        b.putStringArrayList("paths", paths);
        return b;
    }

    public static void delete(File file) throws IOException {

        if (file.isDirectory()) {

            // directory is empty, then delete it
            if (file.list().length == 0) {

                file.delete();
                System.out.println("Directory is deleted : "
                        + file.getAbsolutePath());

            } else {

                // list all the directory contents
                String files[] = file.list();

                for (String temp : files) {
                    // construct the file structure
                    File fileDelete = new File(file, temp);

                    // recursive delete
                    delete(fileDelete);
                }

                // check the directory again, if empty then delete it
                if (file.list().length == 0) {
                    file.delete();
                    System.out.println("Directory is deleted : "
                            + file.getAbsolutePath());
                }
            }

        } else {
            // if file, then delete it
            file.delete();
            System.out.println("File is deleted : " + file.getAbsolutePath());
        }
    }

    public boolean deletefiles(File f) {

        // make sure directory exists
        if (!f.exists()) {

            System.out.println("Directory does not exist.");
            return false;

        } else {

            try {

                delete(f);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

    }

    public boolean rename(File f, String name) {
        String newname = f.getParent() + "/" + name;
        if(f.getParentFile().canWrite()){
        boolean b = f.renameTo(new File(newname));}
        else{try{RootTools.getShell(true).add(new Command(0,"mv "+f.getPath()+" "+newname) {
            @Override
            public void commandOutput(int i, String s) {
                System.out.println(s);
            }

            @Override
            public void commandTerminated(int i, String s) {

            }

            @Override
            public void commandCompleted(int i, int i2) {

            }
        });}catch (Exception e){return false;}}
        return true;
    }

    public boolean canListFiles(File f) {
        try {
            if (f.canRead() && f.isDirectory())
                return true;
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void openFile(final File f, final MainActivity m) {
        if (Icons.isText(f.getPath())) {
            Intent i = new Intent(m, TextReader.class);
            i.putExtra("path", f.getPath());
            m.startActivity(i);
        } else if (Icons.isCode(f.getName())) {
            Intent i = new Intent(m, TextReader.class);
            i.putExtra("path", f.getPath());
            m.startActivity(i);
        } else {
            try {
                openunknown(f, m);
            } catch (Exception e) {
                Crouton.makeText(m, getString(m, R.string.noappfound), Style.ALERT).show();
                openWith(f, m);
            }
        }
    }

    public Layoutelements newElement(Drawable i, String d) {
        Layoutelements item = new Layoutelements(i, new File(d).getName(), d);
        return item;
    }

    public ArrayList<File> toFileArray(ArrayList<String> a) {
        ArrayList<File> b = new ArrayList<File>();
        for (int i = 0; i < a.size(); i++) {
            b.add(new File(a.get(i)));
        }
        return b;
    }

    public ArrayList<String> toStringArray(ArrayList<File> a) {
        ArrayList<String> b = new ArrayList<String>();
        for (int i = 0; i < a.size(); i++) {
            b.add(a.get(i).getPath());
        }
        return b;
    }

    public void showNameDialog(final MainActivity m, final ArrayList<String> b, final String current) {
        AlertDialog.Builder a = new AlertDialog.Builder(m);
        View v = m.getLayoutInflater().inflate(R.layout.dialog, null);
        final EditText e = (EditText) v.findViewById(R.id.newname);
        e.setText("Newzip.zip");
        a.setView(v);
        a.setTitle(getString(m, R.string.enterzipname));
        e.setHint(getString(m, R.string.enterzipname));
        a.setPositiveButton(getString(m, R.string.create), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface p1, int p2) {
                Intent intent2 = new Intent(m, ZipTask.class);
                String name = current + "/" + e.getText().toString();
                intent2.putExtra("name", name);
                intent2.putExtra("files", b);
                m.startService(intent2);
                // TODO: Implement this method
            }
        });
        a.setNegativeButton(getString(m, R.string.cancel), null);
        a.show();
    }

    public void longClickSearchItem(final Main main, String files) {
        final File f = new File(files);
        AlertDialog.Builder ba = new AlertDialog.Builder(main.getActivity());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                main.getActivity(), android.R.layout.select_dialog_item);
        Toast.makeText(main.getActivity(), files, Toast.LENGTH_SHORT).show();
        ba.setTitle(f.getName());
        adapter.add(getString(main.getActivity(), R.string.openparent));
        adapter.add(getString(main.getActivity(), R.string.openwith));
        adapter.add(getString(main.getActivity(), R.string.about));
        adapter.add(getString(main.getActivity(), R.string.share));
        adapter.add(getString(main.getActivity(), R.string.compress));
        if (!f.isDirectory() && f.getName().endsWith(".zip"))
            adapter.add(getString(main.getActivity(), R.string.extract));
        ba.setAdapter(adapter, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface p1, int p2) {
                switch (p2) {
                    case 0:
                        main.loadlist(new File(main.slist.get(p2).getDesc()).getParentFile(), true);
                        break;
                    case 1:
                        openunknown(f, main.getActivity());
                        break;
                    case 2:
                        showProps(f, main.getActivity());
                        break;
                    case 3:
                        Intent i = new Intent();
                        i.setAction(Intent.ACTION_SEND);
                        i.setType("*/*");
                        i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
                        main.startActivity(i);
                        break;
                    case 4:
                        ArrayList<String> copies1 = new ArrayList<String>();
                        copies1.add(f.getPath());
                        showNameDialog((MainActivity) main.getActivity(), copies1, main.current);

                        break;
                    case 5:
                        Intent intent = new Intent(main.getActivity(), ExtractService.class);
                        intent.putExtra("zip", f.getPath());
                        main.getActivity().startService(intent);
                        break;
                }
            }
        });
        ba.show();
    }

    public void showSortDialog(final Main m) {
        String[] sort = m.getResources().getStringArray(R.array.sortby);
        int current = Integer.parseInt(m.Sp.getString("sortby", "0"));
        AlertDialog.Builder a = new AlertDialog.Builder(m.getActivity());

        a.setSingleChoiceItems(new ArrayAdapter<String>(m.getActivity(), android.R.layout.select_dialog_singlechoice, sort), current, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {

                m.Sp.edit().putString("sortby", "" + i).commit();
                m.getSortModes();
                m.loadlist(new File(m.current), false);
                dialog.cancel();
            }
        });
        a.setTitle(getString(m.getActivity(), R.string.sortby));
        a.setNegativeButton(getString(m.getActivity(), R.string.cancel), null);
        a.show();
    }

    public void showDirectorySortDialog(final Main m) {
        String[] sort = m.getResources().getStringArray(R.array.directorysortmode);
        AlertDialog.Builder a = new AlertDialog.Builder(m.getActivity());
        int current = Integer.parseInt(m.Sp.getString("dirontop", "0"));

        a.setSingleChoiceItems(new ArrayAdapter<String>(m.getActivity(), android.R.layout.select_dialog_singlechoice, sort), current, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                m.Sp.edit().putString("dirontop", "" + i).commit();
                m.getSortModes();
                m.loadlist(new File(m.current), false);
            }
        });

        a.setTitle(getString(m.getActivity(), R.string.directorysort));
        a.setNegativeButton(getString(m.getActivity(), R.string.cancel), null);
        a.show();
    }

    public void showHistoryDialog(final Main m) {
        final ArrayList<String> paths = m.history.readTable();

        AlertDialog.Builder a = new AlertDialog.Builder(m.getActivity());
        a.setTitle(getString(m.getActivity(), R.string.history));
        DialogAdapter adapter = new DialogAdapter(m.getActivity(), R.layout.bookmarkrow, toFileArray(paths));
        a.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                m.loadlist(new File(paths.get(i)), true);
            }
        });
        a.setNegativeButton(getString(m.getActivity(), R.string.cancel), null);
        a.show();

    }

    public void showBookmarkDialog(final Main m, Shortcuts sh) {
        try {
            final ArrayList<File> fu = sh.readS();

            AlertDialog.Builder ba = new AlertDialog.Builder(m.getActivity());
            ba.setTitle(getString(m.getActivity(), R.string.books));

            DialogAdapter adapter = new DialogAdapter(
                    m.getActivity(), android.R.layout.select_dialog_item, fu);
            ba.setAdapter(adapter, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface p1, int p2) {
                    final File f = fu.get(p2);
                    if (f.isDirectory()) {

                        m.loadlist(f, false);
                    } else {
                        openFile(f, (MainActivity) m.getActivity());
                    }
                }
            });
            ba.setNegativeButton(getString(m.getActivity(), R.string.cancel), null);
            ba.show();
        } catch (IOException e) {
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        }
    }
}