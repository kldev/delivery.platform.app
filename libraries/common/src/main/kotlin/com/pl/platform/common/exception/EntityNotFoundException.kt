package com.pl.platform.common.exception

class EntityNotFoundException(entityType: EntityType,val id: Any)
    : RuntimeException( "$entityType with id $id was not found")