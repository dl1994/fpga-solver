package hr.dlatecki.clb.args;

public class FieldKey<T> {
    
    private final String name;
    private final T defaultValue;
    
    public FieldKey(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }
    
    public T getDefaultValue() {
        return defaultValue;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (obj == null) {
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        @SuppressWarnings("unchecked")
        FieldKey<T> other = (FieldKey<T>) obj;
        
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        
        return true;
    }
}
