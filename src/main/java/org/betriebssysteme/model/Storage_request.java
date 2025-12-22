package org.betriebssysteme.model;

/**
 * Record for a storage request
 * @param materialType type of material
 * @param amount the amount of material
 * @param priority priority of the request
 *                 1 - highest priority
 *                 5 - lowest priority
 */
public record Storage_request(MaterialType materialType, int amount, int priority) {}

