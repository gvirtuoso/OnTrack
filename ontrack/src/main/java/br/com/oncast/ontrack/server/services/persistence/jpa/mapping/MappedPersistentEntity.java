package br.com.oncast.ontrack.server.services.persistence.jpa.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.com.oncast.ontrack.server.services.persistence.jpa.entities.ModelActionEntity;

/**
 * This annotation marks a class that wants to be translated and persisted. You have to define a target class for which the annotated class will be translated.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MappedPersistentEntity {
	Class<? extends ModelActionEntity> value();
}
