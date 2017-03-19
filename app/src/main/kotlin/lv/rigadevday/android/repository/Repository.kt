package lv.rigadevday.android.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import durdinapps.rxfirebase2.DataSnapshotMapper
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Flowable
import io.reactivex.Single
import lv.rigadevday.android.repository.model.Root
import lv.rigadevday.android.repository.model.other.Venue
import lv.rigadevday.android.repository.model.partners.Partners
import lv.rigadevday.android.repository.model.schedule.Rating
import lv.rigadevday.android.repository.model.schedule.Schedule
import lv.rigadevday.android.repository.model.schedule.Session
import lv.rigadevday.android.repository.model.schedule.Timeslot
import lv.rigadevday.android.repository.model.speakers.Speaker
import lv.rigadevday.android.utils.auth.AuthStorage
import lv.rigadevday.android.utils.bindSchedulers

/**
 * All of the observables provided by repository are non-closable so it is mandatory
 * to unsubscribe any subscription when closing screen to prevent memory leak.
 */
class Repository(val authStorage: AuthStorage, val dataCache: DataCache) {

    private val database: DatabaseReference by lazy {
        val ref = FirebaseDatabase.getInstance().reference
        ref.keepSynced(true)
        return@lazy ref
    }

    private fun getCache(predicate: () -> Boolean): Single<DataCache> =
        if (predicate()) Single.just(dataCache)
        else updateCache()

    // Basic requests
    fun updateCache(): Single<DataCache> = RxFirebaseDatabase.observeSingleValueEvent(
        database,
        DataSnapshotMapper.of(Root::class.java)
    ).firstElement().map {
        dataCache.update(it)
    }.toSingle()

    fun speakers(): Flowable<Speaker> = getCache { dataCache.speakers.isNotEmpty() }
        .flattenAsFlowable { it.speakers.values }
        .bindSchedulers()

    fun speaker(id: Int): Single<Speaker> = getCache { dataCache.speakers.isNotEmpty() }
        .map { it.speakers.getValue(id) }
        .bindSchedulers()

    fun schedule(): Flowable<Schedule> = getCache { dataCache.schedule.isNotEmpty() }
        .flattenAsFlowable { it.schedule.values }
        .bindSchedulers()

    fun partners(): Flowable<Partners> = getCache { dataCache.partners.isNotEmpty() }
        .flattenAsFlowable { it.partners }
        .bindSchedulers()

    fun venues(): Flowable<Venue> = getCache { dataCache.venues.isNotEmpty() }
        .flattenAsFlowable { it.venues }
        .bindSchedulers()

    fun venue(id: Int): Single<Venue> = getCache { dataCache.venues.isNotEmpty() }
        .map { it.venues[id] }
        .bindSchedulers()

    fun sessions(): Flowable<Session> = getCache { dataCache.sessions.isNotEmpty() }
        .flattenAsFlowable { it.sessions.values }
        .bindSchedulers()

    fun session(id: Int): Single<Session> = getCache { dataCache.sessions.isNotEmpty() }
        .map { it.sessions.getValue(id) }
        .bindSchedulers()

    fun scheduleDayTimeslots(dateCode: String): Flowable<Timeslot> = getCache { dataCache.schedule.isNotEmpty() }
        .flattenAsFlowable { it.schedule.getValue(dateCode).timeslots }
        .bindSchedulers()

    // Read-Write stuff
    private fun sessionRating() = database.child("userFeedbacks").child(authStorage.uId)

    fun rating(sessionId: Int): Single<Rating> = if (authStorage.hasLogin) {
        RxFirebaseDatabase.observeSingleValueEvent(
            sessionRating().child(sessionId.toString()),
            Rating::class.java
        ).onErrorReturnItem(Rating()).firstOrError()
    } else Single.just(Rating())

    fun saveRating(sessionId: Int, rating: Rating) {
        if (authStorage.hasLogin) {
            sessionRating().child(sessionId.toString()).setValue(rating)
        }
    }
}
