package nlp.preprocess;

import java.io.*;
import java.util.*;

/**
 * Created by Saeed on 2/16/2017.
 */
public class SerializableReader<E extends Serializable> implements Iterator<E>, Closeable {

    ObjectInputStream objectinputstream = null;
    E readCase;

    public SerializableReader(String fileAddress){

        try {
            FileInputStream streamIn = new FileInputStream(fileAddress);
            objectinputstream = new ObjectInputStream(streamIn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        if(objectinputstream != null){
            objectinputstream .close();
        }
    }

    @Override
    public boolean hasNext() {
        readCase = null;
        try {
            readCase = (E) objectinputstream.readObject();
            return true;
        }catch (EOFException|NullPointerException e){
            return false;
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public E next() {
        return readCase;
    }



    public static <E extends Serializable> Set<E> getObjectSet(String serializedFileAddress) throws IOException {
        return new HashSet<E>(getObjectList(serializedFileAddress));
    }

    public static <E extends Serializable> List<E> getObjectList(String serializedFileAddress) throws IOException {
        return getObjectList(serializedFileAddress,-1);
    }

    public static <E extends Serializable> List<E> getObjectList(String serializedFileAddress, int lineCount) throws IOException {
        List<E> objectList=new ArrayList<E>();
        SerializableReader<E> objReader=new SerializableReader<E>(serializedFileAddress);
        int lineCounter=0;
        while (objReader.hasNext()){
            objectList.add(objReader.next());
            lineCount++;
            if(lineCounter>0 && lineCount>=lineCounter){
                break;
            }
        }
        objReader.close();
        return objectList;
    }
}
