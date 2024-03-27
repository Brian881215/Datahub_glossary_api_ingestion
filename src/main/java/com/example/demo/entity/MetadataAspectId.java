package com.example.demo.entity;
import java.io.Serializable;
import java.util.Objects;

public class MetadataAspectId implements Serializable {
    private static final long serialVersionUID = 1L;
	private String urn;
    private String aspect;
    private Long version;

    // Getters, setters, hashCode, and equals methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetadataAspectId that = (MetadataAspectId) o;
        return urn.equals(that.urn) &&
               aspect.equals(that.aspect) &&
               version.equals(that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(urn, aspect, version);
    }

    // Getters and setters
}
