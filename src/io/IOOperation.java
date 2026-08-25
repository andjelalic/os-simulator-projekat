package io;

public class IOOperation {
    private IOType type;
    private String data;
    private int duration;

    public IOOperation(IOType type, String data, int duration){
        if(type.equals(IOType.WRITE) && (data == null || data.isEmpty())){
            throw new IllegalArgumentException("No data provided to write.");
        }
        if(type.equals(IOType.READ) && (data != null)){
            throw new IllegalArgumentException("Too many arguments for the read operation.");
        }
        if(duration <= 0){
            throw new IllegalArgumentException("An operation must be at least one time unit long.");
        }

        this.type = type;
        this.data = data;
        this.duration = duration;
    }

    public IOOperation(IOType type, int duration){
        this(type, null, duration);
    }

    public IOType getType(){
        return type;
    }

    public String getData(){
        return data;
    }

    public void setData(String d){
        data = d;
    }

    public int getDuration(){
        return duration;
    }
}
