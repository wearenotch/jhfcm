import { Injectable } from '@angular/core';
import { AngularFireMessaging } from '@angular/fire/messaging';
import { AlertService } from 'app/core/util/alert.service';
import { Observable, Subject } from 'rxjs';

import { HttpClient } from '@angular/common/http';
import { SERVER_API_URL } from 'app/app.constants';
import { IJoke, Joke } from 'app/firebase/joke.model';
import { publicVapidKey } from 'app/firebase/firebase.constants';
import { ITopicSubscriptionResult } from 'app/firebase/topic-subscription-result.model';


@Injectable({ providedIn: 'root' })
export class FcmService {
  public resourceUrl = SERVER_API_URL + 'api/fcm/registration';
  fcmToken!: string | null;

  // storage for FCM messages ...
  public jokes = new Subject<IJoke>();

  constructor(
    protected http: HttpClient,
    protected angularFireMessaging: AngularFireMessaging,
    protected alertService: AlertService
  ) {
    this.angularFireMessaging.usePublicVapidKey(publicVapidKey).then(() => {
      this.getFcmToken();
    });
    this.angularFireMessaging.messages.subscribe((message: any) => {
      const joke = new Joke(message['data'].id, message['data'].joke, message['data'].seq, message['data'].ts);
      this.jokes.next(joke)
    });
  }

  getFcmToken(): void {
    // 1. do we already have a token ...
    if (this.fcmToken != null) {
      // eslint-disable-next-line no-console
      console.log('Token already acquired! Using the existing one: token=', this.fcmToken);
      return;
    }
    // 2 if not ask user for permision and get tokn for further use ...
    this.angularFireMessaging.requestToken.subscribe(
      token => {
        this.alertService.addAlert({ type: 'success', message: 'fcm.permission-granted' });
        this.fcmToken = token;
      },
      err => {
        this.alertService.addAlert({ type: 'warning', message: 'fcm.permission-failed' });
        // eslint-disable-next-line no-console
        console.error('Unable to get permission to notify.', err);
      }
    );
  }

  subscribeToJokeTopic(): void {
    this.http.post<ITopicSubscriptionResult>(`${this.resourceUrl}`, this.fcmToken, { observe: 'body' }).subscribe(
      (result: ITopicSubscriptionResult) => {
        if (result.success) {
          // eslint-disable-next-line no-console
          console.log('--> Subscribed to topic: ', result.topic);
          this.alertService.addAlert({ type: 'success', message: 'fcm.topic.subscribe-success' });
        } else {
          // eslint-disable-next-line no-console
          console.error('Failed to subscribe to Topic.', result.topic, result.errorCode);
          this.alertService.addAlert({ type: 'warning', message: 'fcm.topic.subscribe-failed' });
        }
      }
    );
  }

  unsubscribeFromJokeTopic(): void {
    this.http.put<ITopicSubscriptionResult>(`${this.resourceUrl}`, this.fcmToken, { observe: 'body' }).subscribe(
      (result: ITopicSubscriptionResult) => {
        if (result.success) {
          // eslint-disable-next-line no-console
          console.log('--> Unubscribed from topic: ', result.topic);
          this.alertService.addAlert({ type: 'success', message: 'fcm.topic.unsubscribe-success' });
        } else {
          // eslint-disable-next-line no-console
          console.error('Failed to unsubscribe from Topic.', result.topic, result.errorCode);
          this.alertService.addAlert({ type: 'warning', message: 'fcm.topic.unsubscribe-failed' });
        }
      }
    );
  }

}