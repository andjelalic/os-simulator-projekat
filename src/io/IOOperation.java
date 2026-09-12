package io;

public class IOOperation {
    private IOType type;
    private String data;
    private int duration;

    public IOOperation(IOType type, String data, int duration){
        if(type.equals(IOType.WRITE) && (data == null || data.isEmpty())){
            throw new IllegalArgumentException("Nema podataka za upis.");
        }
        if(type.equals(IOType.READ) && (data != null)){
            throw new IllegalArgumentException("Previše argumenata za operaciju čitanja.");
        }
        if(duration <= 0){
            throw new IllegalArgumentException("Operacija treba da traje barem jednu vremensku jedinicu.");
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
