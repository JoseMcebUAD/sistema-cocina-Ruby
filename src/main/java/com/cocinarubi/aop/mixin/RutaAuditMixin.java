package com.cocinarubi.aop.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.locationtech.jts.geom.Geometry;

public abstract class RutaAuditMixin {
    @JsonIgnore abstract Geometry getBoundary();
}
