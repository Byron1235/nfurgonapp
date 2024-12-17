package com.example.nfurgonapp.Remote

import com.example.nfurgonapp.Model.FCMResponse
import com.example.nfurgonapp.Model.FCMSendData
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMService {
        @Headers(
            "Content-Type:application/json",
            "Authorization:key=Bearer ya29.c.c0ASRK0GZkzmCDVBzl7L_03tpOqgJSekQOZ7UkRinZ13lvGSx5dFi4PGRyYW7kdaUF4xVLHmUzI_JSSOSHJos9pWL7wo9ur-0a-1zInooe7XkJRrt4OtHygYqKZWLgwro4cx5Bi39HKVN1S6ql3shMZTrzYOm2njA1B7K_uYKzSTJxZ6p7aoxJnmoaZVoDSz2k5bMi-9NjxrlAZSvSu22xK9zcsoe7Hz4LaXNdCVmJgsqGtaWB-ea5e3E04LQsiiQu2vaQpFRtQqpx6nrls4jbn3UryR0v0dLu2mp-lqDxo4dBzd90aYyHZlG9WyECsLHKIejxfhIRap1s2dKB_10ADnVQxsRwe_odiLblykj0IHVT5QRbFO2tnO0zT385PYQvZb7hcdYt5FOpv3W8-jxQ8XZfhqJWZ9noijOjJfZie2ejgVnqOFtSdjZItOXwOmmumzQwJkhsjYi8t3exdzij8BvfsSljvJR6JXf5emY72v0vi3c4tl6kyfn7aBtl6Z2aI5yy85gi5adsY9_UMo3F2w89eetdZk5WFQ94j9M6MhqRWxSM9y82UgXQz6blJ01o92i6gi2QsmQO-MuYyUIblRVjZ_kSc4h08ir6_pWhudddqubelSeigXnjaWoWoiurd4aIYldFitvUMIIcqjJqQqrcJMfeMz96-93zibih7qze4tRkwg9wqwtrW719ihnyecf9393lbOkx3SBX1YvwYJtaB1cqWmhUbkrX0ZyY22s9ZcFByinMI7Vcwt9BSWhZn8Bvnnsu_47Uyk06YvjmxcUjzmIrqnUlhYxdlcwBreW9f6OVd7Rz0t1nXm8z17giZSVXnX-cqdOvYbh1JYvmrnvjenhOOo9r3QexXp5WZyvnetIwQo4sFbYhM8h-FpQ4kgBOZ7w69f7ov7r9e9aBM4k_Ql8mubiYmq8xkmIIWBsSaFYp1kq_9sw_JkY8XgUiS-hV29w37sWw3oYQB2vu1iuVYJtpBZO62iWJvhSRoVtnYO_BeQJnX8c"
        )
        @POST("fcm/send")
        fun sendNotification(@Body body: FCMSendData?): Observable<FCMResponse>

}