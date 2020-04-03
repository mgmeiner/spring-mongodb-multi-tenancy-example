package de.mgmeiner.examples.mongo.multitenancy.tenant;

import com.mongodb.ClientSessionOptions;
import com.mongodb.ReadPreference;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.ClientSession;
import com.mongodb.reactivestreams.client.MongoCollection;
import org.bson.Document;
import org.reactivestreams.Publisher;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.mapreduce.MapReduceOptions;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

final class MultiTenancyReactiveMongoTemplate implements ReactiveMongoOperations, ApplicationContextAware {

    private Map<String, ReactiveMongoTemplate> delegates = new HashMap<>();

    public MultiTenancyReactiveMongoTemplate(
            List<MultiTenancyReactiveMongoDatabaseFactory> multiTenancyReactiveMongoDatabaseFactories,
            MongoConverter mongoConverter
    ) {
        for (var multiTenancyReactiveMongoDatabaseFactory : multiTenancyReactiveMongoDatabaseFactories) {
            var reactiveMongoTemplate = new ReactiveMongoTemplate(
                    multiTenancyReactiveMongoDatabaseFactory,
                    mongoConverter);

            delegates.put(multiTenancyReactiveMongoDatabaseFactory.getTenantId(), reactiveMongoTemplate);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        eachDelegate(rmt -> rmt.setApplicationContext(applicationContext));
    }

    @Override
    public ReactiveIndexOperations indexOps(String collectionName) {
        throw new UnsupportedOperationException("currently not supported");
    }

    @Override
    public ReactiveIndexOperations indexOps(Class<?> entityClass) {
        throw new UnsupportedOperationException("currently not supported");
    }

    @Override
    public Mono<Document> executeCommand(String jsonCommand) {
        return getDelegate().flatMap(it -> it.executeCommand(jsonCommand));
    }

    @Override
    public Mono<Document> executeCommand(Document command) {
        return getDelegate().flatMap(it -> it.executeCommand(command));
    }

    @Override
    public Mono<Document> executeCommand(Document command, ReadPreference readPreference) {
        return getDelegate().flatMap(it -> it.executeCommand(command, readPreference));
    }

    @Override
    public <T> Flux<T> execute(Class<?> entityClass, ReactiveCollectionCallback<T> action) {
        return getDelegate().flatMapMany(it -> it.execute(entityClass, action));
    }

    @Override
    public <T> Flux<T> execute(ReactiveDatabaseCallback<T> action) {
        return getDelegate().flatMapMany(it -> it.execute(action));
    }

    @Override
    public <T> Flux<T> execute(String collectionName, ReactiveCollectionCallback<T> callback) {
        return getDelegate().flatMapMany(it -> it.execute(collectionName, callback));
    }

    @Override
    public ReactiveSessionScoped inTransaction() {
        throw new UnsupportedOperationException("not working");
    }

    @Override
    public ReactiveSessionScoped inTransaction(Publisher<ClientSession> sessionProvider) {
        throw new UnsupportedOperationException("not working");
    }

    @Override
    public ReactiveMongoOperations withSession(ClientSession session) {
        throw new UnsupportedOperationException("not working");
    }

    @Override
    public ReactiveSessionScoped withSession(ClientSessionOptions sessionOptions) {
        throw new UnsupportedOperationException("not working");
    }

    @Override
    public ReactiveSessionScoped withSession(Publisher<ClientSession> sessionProvider) {
        return null;
    }

    @Override
    public <T> Mono<MongoCollection<Document>> createCollection(Class<T> entityClass) {
        return getDelegate().flatMap(it -> it.createCollection(entityClass));
    }

    @Override
    public <T> Mono<MongoCollection<Document>> createCollection(Class<T> entityClass, CollectionOptions collectionOptions) {
        return getDelegate().flatMap(it -> it.createCollection(entityClass, collectionOptions));
    }

    @Override
    public Mono<MongoCollection<Document>> createCollection(String collectionName) {
        return getDelegate().flatMap(it -> it.createCollection(collectionName));
    }

    @Override
    public Mono<MongoCollection<Document>> createCollection(String collectionName, CollectionOptions collectionOptions) {
        return getDelegate().flatMap(it -> it.createCollection(collectionName, collectionOptions));
    }

    @Override
    public MongoCollection<Document> getCollection(String collectionName) {
        throw new UnsupportedOperationException("not working");
    }

    @Override
    public <T> Mono<Boolean> collectionExists(Class<T> entityClass) {
        return getDelegate().flatMap(it -> it.collectionExists(entityClass));
    }

    @Override
    public Mono<Boolean> collectionExists(String collectionName) {
        return getDelegate().flatMap(it -> it.collectionExists(collectionName));
    }

    @Override
    public <T> Mono<Void> dropCollection(Class<T> entityClass) {
        return getDelegate().flatMap(it -> it.dropCollection(entityClass));
    }

    @Override
    public Mono<Void> dropCollection(String collectionName) {
        return getDelegate().flatMap(it -> it.dropCollection(collectionName));
    }

    @Override
    public Flux<String> getCollectionNames() {
        return getDelegate().flatMapMany(ReactiveMongoTemplate::getCollectionNames);
    }

    @Override
    public <T> Mono<T> findOne(Query query, Class<T> entityClass) {
        return getDelegate().flatMap(it -> it.findOne(query, entityClass));
    }

    @Override
    public <T> Mono<T> findOne(Query query, Class<T> entityClass, String collectionName) {
        return getDelegate().flatMap(it -> it.findOne(query, entityClass, collectionName));
    }

    @Override
    public Mono<Boolean> exists(Query query, Class<?> entityClass) {
        return getDelegate().flatMap(it -> it.exists(query, entityClass));
    }

    @Override
    public Mono<Boolean> exists(Query query, String collectionName) {
        return getDelegate().flatMap(it -> it.exists(query, collectionName));
    }

    @Override
    public Mono<Boolean> exists(Query query, Class<?> entityClass, String collectionName) {
        return getDelegate().flatMap(it -> it.exists(query, entityClass, collectionName));
    }

    @Override
    public <T> Flux<T> find(Query query, Class<T> entityClass) {
        return getDelegate().flatMapMany(it -> it.find(query, entityClass));
    }

    @Override
    public <T> Flux<T> find(Query query, Class<T> entityClass, String collectionName) {
        return getDelegate().flatMapMany(it -> it.find(query, entityClass, collectionName));
    }

    @Override
    public <T> Mono<T> findById(Object id, Class<T> entityClass) {
        return getDelegate().flatMap(it -> it.findById(id, entityClass));
    }

    @Override
    public <T> Mono<T> findById(Object id, Class<T> entityClass, String collectionName) {
        return getDelegate().flatMap(it -> it.findById(id, entityClass, collectionName));
    }

    @Override
    public <T> Flux<T> findDistinct(Query query, String field, Class<?> entityClass, Class<T> resultClass) {
        return getDelegate().flatMapMany(it -> it.findDistinct(query, field, entityClass, resultClass));
    }

    @Override
    public <T> Flux<T> findDistinct(Query query, String field, String collectionName, Class<?> entityClass, Class<T> resultClass) {
        return getDelegate().flatMapMany(it -> it.findDistinct(query, field, collectionName, entityClass, resultClass));
    }

    @Override
    public <O> Flux<O> aggregate(TypedAggregation<?> aggregation, String inputCollectionName, Class<O> outputType) {
        return getDelegate().flatMapMany(it -> it.aggregate(aggregation, inputCollectionName, outputType));
    }

    @Override
    public <O> Flux<O> aggregate(TypedAggregation<?> aggregation, Class<O> outputType) {
        return getDelegate().flatMapMany(it -> it.aggregate(aggregation, outputType));
    }

    @Override
    public <O> Flux<O> aggregate(Aggregation aggregation, Class<?> inputType, Class<O> outputType) {
        return getDelegate().flatMapMany(it -> it.aggregate(aggregation, inputType, outputType));
    }

    @Override
    public <O> Flux<O> aggregate(Aggregation aggregation, String collectionName, Class<O> outputType) {
        return getDelegate().flatMapMany(it -> it.aggregate(aggregation, collectionName, outputType));
    }


    @Override
    public <T> Flux<GeoResult<T>> geoNear(NearQuery near, Class<T> entityClass) {
        return getDelegate().flatMapMany(it -> it.geoNear(near, entityClass));
    }

    @Override
    public <T> Flux<GeoResult<T>> geoNear(NearQuery near, Class<T> entityClass, String collectionName) {
        return getDelegate().flatMapMany(it -> it.geoNear(near, entityClass, collectionName));
    }

    @Override
    public <T> Mono<T> findAndModify(Query query, Update update, Class<T> entityClass) {
        return getDelegate().flatMap(it -> it.findAndModify(query, update, entityClass));
    }

    @Override
    public <T> Mono<T> findAndModify(Query query, Update update, Class<T> entityClass, String collectionName) {
        return getDelegate().flatMap(it -> it.findAndModify(query, update, entityClass, collectionName));
    }

    @Override
    public <T> Mono<T> findAndModify(Query query, Update update, FindAndModifyOptions options, Class<T> entityClass) {
        return getDelegate().flatMap(it -> it.findAndModify(query, update, options, entityClass));
    }

    @Override
    public <T> Mono<T> findAndModify(Query query, Update update, FindAndModifyOptions options, Class<T> entityClass, String collectionName) {
        return getDelegate().flatMap(it -> it.findAndModify(query, update, options, entityClass, collectionName));
    }

    @Override
    public <S, T> Mono<T> findAndReplace(Query query, S replacement, FindAndReplaceOptions options, Class<S> entityType, String collectionName, Class<T> resultType) {
        return getDelegate().flatMap(it -> it.findAndReplace(query, replacement, options, entityType, collectionName, resultType));
    }

    @Override
    public <T> Mono<T> findAndRemove(Query query, Class<T> entityClass) {
        return getDelegate().flatMap(it -> it.findAndRemove(query, entityClass));
    }

    @Override
    public <T> Mono<T> findAndRemove(Query query, Class<T> entityClass, String collectionName) {
        return getDelegate().flatMap(it -> it.findAndRemove(query, entityClass, collectionName));
    }

    @Override
    public Mono<Long> count(Query query, Class<?> entityClass) {
        return getDelegate().flatMap(it -> it.count(query, entityClass));
    }

    @Override
    public Mono<Long> count(Query query, String collectionName) {
        return getDelegate().flatMap(it -> it.count(query, collectionName));
    }

    @Override
    public Mono<Long> count(Query query, Class<?> entityClass, String collectionName) {
        return getDelegate().flatMap(it -> it.count(query, entityClass, collectionName));
    }

    @Override
    public <T> Flux<T> insertAll(Mono<? extends Collection<? extends T>> batchToSave, Class<?> entityClass) {
        return getDelegate().flatMapMany(it -> it.insertAll(batchToSave, entityClass));
    }

    @Override
    public <T> Flux<T> insertAll(Mono<? extends Collection<? extends T>> batchToSave, String collectionName) {
        return getDelegate().flatMapMany(it -> it.insertAll(batchToSave, collectionName));
    }

    @Override
    public <T> Mono<T> insert(T objectToSave) {
        return getDelegate().flatMap(it -> it.insert(objectToSave));
    }

    @Override
    public <T> Mono<T> insert(T objectToSave, String collectionName) {
        return getDelegate().flatMap(it -> it.insert(objectToSave, collectionName));
    }

    @Override
    public <T> Flux<T> insert(Collection<? extends T> batchToSave, Class<?> entityClass) {
        return getDelegate().flatMapMany(it -> it.insert(batchToSave, entityClass));
    }

    @Override
    public <T> Flux<T> insert(Collection<? extends T> batchToSave, String collectionName) {
        return getDelegate().flatMapMany(it -> it.insert(batchToSave, collectionName));
    }

    @Override
    public <T> Flux<T> insertAll(Collection<? extends T> objectsToSave) {
        return getDelegate().flatMapMany(it -> it.insertAll(objectsToSave));
    }

    @Override
    public <T> Flux<T> insertAll(Mono<? extends Collection<? extends T>> objectsToSave) {
        return getDelegate().flatMapMany(it -> it.insertAll(objectsToSave));
    }

    @Override
    public <T> Mono<T> save(Mono<? extends T> objectToSave) {
        return getDelegate().flatMap(it -> it.save(objectToSave));
    }

    @Override
    public <T> Mono<T> save(Mono<? extends T> objectToSave, String collectionName) {
        return getDelegate().flatMap(it -> it.save(objectToSave, collectionName));
    }

    @Override
    public <T> Mono<T> save(T objectToSave) {
        return getDelegate().flatMap(it -> it.save(objectToSave));
    }

    @Override
    public <T> Mono<T> save(T objectToSave, String collectionName) {
        return getDelegate().flatMap(it -> it.save(objectToSave, collectionName));
    }

    @Override
    public Mono<UpdateResult> upsert(Query query, Update update, Class<?> entityClass) {
        return getDelegate().flatMap(it -> it.upsert(query, update, entityClass));
    }

    @Override
    public Mono<UpdateResult> upsert(Query query, Update update, String collectionName) {
        return getDelegate().flatMap(it -> it.upsert(query, update, collectionName));
    }

    @Override
    public Mono<UpdateResult> upsert(Query query, Update update, Class<?> entityClass, String collectionName) {
        return getDelegate().flatMap(it -> it.upsert(query, update, entityClass, collectionName));
    }

    @Override
    public Mono<UpdateResult> updateFirst(Query query, Update update, Class<?> entityClass) {
        return getDelegate().flatMap(it -> it.updateFirst(query, update, entityClass));
    }

    @Override
    public Mono<UpdateResult> updateFirst(Query query, Update update, Class<?> entityClass, String collectionName) {
        return getDelegate().flatMap(it -> it.updateFirst(query, update, entityClass, collectionName));
    }

    @Override
    public Mono<UpdateResult> updateMulti(Query query, Update update, Class<?> entityClass) {
        return getDelegate().flatMap(it -> it.updateMulti(query, update, entityClass));
    }

    @Override
    public Mono<UpdateResult> updateMulti(Query query, Update update, String collectionName) {
        return getDelegate().flatMap(it -> it.updateMulti(query, update, collectionName));
    }

    @Override
    public Mono<UpdateResult> updateMulti(Query query, Update update, Class<?> entityClass, String collectionName) {
        return getDelegate().flatMap(it -> it.updateMulti(query, update, entityClass, collectionName));
    }

    @Override
    public Mono<DeleteResult> remove(Mono<?> objectToRemove) {
        return getDelegate().flatMap(it -> it.remove(objectToRemove));
    }

    @Override
    public Mono<DeleteResult> remove(Mono<?> objectToRemove, String collectionName) {
        return getDelegate().flatMap(it -> it.remove(objectToRemove, collectionName));
    }

    @Override
    public Mono<DeleteResult> remove(Object object) {
        return getDelegate().flatMap(it -> it.remove(object));
    }

    @Override
    public Mono<DeleteResult> remove(Object object, String collectionName) {
        return getDelegate().flatMap(it -> it.remove(object, collectionName));
    }

    @Override
    public Mono<DeleteResult> remove(Query query, String collectionName) {
        return getDelegate().flatMap(it -> it.remove(query, collectionName));
    }

    @Override
    public Mono<DeleteResult> remove(Query query, Class<?> entityClass) {
        return getDelegate().flatMap(it -> it.remove(query, entityClass));
    }

    @Override
    public Mono<DeleteResult> remove(Query query, Class<?> entityClass, String collectionName) {
        return getDelegate().flatMap(it -> it.remove(query, entityClass, collectionName));
    }

    @Override
    public <T> Flux<T> findAll(Class<T> entityClass) {
        return getDelegate().flatMapMany(it -> it.findAll(entityClass));
    }

    @Override
    public <T> Flux<T> findAll(Class<T> entityClass, String collectionName) {
        return getDelegate().flatMapMany(it -> it.findAll(entityClass, collectionName));
    }

    @Override
    public <T> Flux<T> findAllAndRemove(Query query, String collectionName) {
        return getDelegate().flatMapMany(it -> it.findAllAndRemove(query, collectionName));
    }

    @Override
    public <T> Flux<T> findAllAndRemove(Query query, Class<T> entityClass) {
        return getDelegate().flatMapMany(it -> it.findAllAndRemove(query, entityClass));
    }

    @Override
    public <T> Flux<T> findAllAndRemove(Query query, Class<T> entityClass, String collectionName) {
        return getDelegate().flatMapMany(it -> it.findAllAndRemove(query, entityClass, collectionName));
    }

    @Override
    public <T> Flux<T> tail(Query query, Class<T> entityClass) {
        return getDelegate().flatMapMany(it -> it.tail(query, entityClass));
    }

    @Override
    public <T> Flux<T> tail(Query query, Class<T> entityClass, String collectionName) {
        return getDelegate().flatMapMany(it -> it.tail(query, entityClass, collectionName));
    }

    @Override
    public <T> Flux<ChangeStreamEvent<T>> changeStream(String database, String collectionName, ChangeStreamOptions options, Class<T> targetType) {
        return getDelegate().flatMapMany(it -> it.changeStream(database, collectionName, options, targetType));
    }

    @Override
    public <T> Flux<T> mapReduce(Query filterQuery, Class<?> domainType, Class<T> resultType, String mapFunction, String reduceFunction, MapReduceOptions options) {
        return getDelegate().flatMapMany(it -> it.mapReduce(filterQuery, domainType, resultType, mapFunction, reduceFunction, options));
    }

    @Override
    public <T> Flux<T> mapReduce(Query filterQuery, Class<?> domainType, String inputCollectionName, Class<T> resultType, String mapFunction, String reduceFunction, MapReduceOptions options) {
        return getDelegate().flatMapMany(it -> it.mapReduce(filterQuery, domainType, inputCollectionName, resultType, mapFunction, reduceFunction, options));
    }

    @Override
    public MongoConverter getConverter() {
        // just return from the first delegate as this should be equal for all delegates.
        return getDefaultDelegate().getConverter();
    }

    @Override
    public String getCollectionName(Class<?> entityClass) {
        // just return from the first delegate as this should be equal for all delegates.
        return getDefaultDelegate().getCollectionName(entityClass);
    }

    @Override
    public <T> ReactiveFind<T> query(Class<T> domainType) {
        throw new UnsupportedOperationException("not working");
    }

    @Override
    public <T> ReactiveUpdate<T> update(Class<T> domainType) {
        throw new UnsupportedOperationException("not working");
    }

    @Override
    public <T> ReactiveRemove<T> remove(Class<T> domainType) {
        throw new UnsupportedOperationException("not working");
    }

    @Override
    public <T> ReactiveInsert<T> insert(Class<T> domainType) {
        throw new UnsupportedOperationException("not working");
    }

    @Override
    public <T> ReactiveAggregation<T> aggregateAndReturn(Class<T> domainType) {
        throw new UnsupportedOperationException("not working");
    }

    @Override
    public <T> ReactiveMapReduce<T> mapReduce(Class<T> domainType) {
        throw new UnsupportedOperationException("not working");
    }

    @Override
    public <T> ReactiveChangeStream<T> changeStream(Class<T> domainType) {
        throw new UnsupportedOperationException("not working");
    }

    @Override
    public <T> Mono<T> insert(Mono<? extends T> objectToSave) {
        return getDelegate().flatMap(it -> it.insert(objectToSave));
    }

    @Override
    public Mono<UpdateResult> updateFirst(Query query, Update update, String collectionName) {
        return getDelegate().flatMap(it -> it.updateFirst(query, update, collectionName));
    }

    private void eachDelegate(Consumer<ReactiveMongoTemplate> action) {
        delegates.values().forEach(action);
    }

    private Mono<ReactiveMongoTemplate> getDelegate() {
        return Mono.subscriberContext().map(ctx -> {
            var tenant = ctx.getOrEmpty("tenant").orElseThrow(() -> new IllegalStateException("tenant must not be null"));
            return delegates.get(tenant);
        });
    }

    private ReactiveMongoTemplate getDefaultDelegate() {
        return delegates.values().stream().findFirst().get();
    }
}
