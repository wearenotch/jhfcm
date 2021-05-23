import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';

import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import { FcmService } from 'app/firebase/fcm.service'
import { Joke } from 'app/firebase/joke.model';

@Component({
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit, OnDestroy {
  account: Account | null = null;
  authSubscription?: Subscription;
  
  latestJoke  = new Joke(-1, "Latest Chuck joke... (comming soon)", 0, undefined);

  constructor(private accountService: AccountService, private router: Router, private fcmService: FcmService) {}

  ngOnInit(): void {
    this.authSubscription = this.accountService.getAuthenticationState().subscribe(account => (this.account = account));
    this.fcmService.jokes.subscribe(joke => {
      // eslint-disable-next-line no-console
      console.log('--> Latest Joke:', joke.joke);
      this.latestJoke = joke;
    });
  }

  isAuthenticated(): boolean {
    return this.accountService.isAuthenticated();
  }

  login(): void {
    this.router.navigate(['/login']);
  }

  ngOnDestroy(): void {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
  }

  // --- FCM methods -----------------------------------------------------------

  subscribeToJokes(): void {
    this.fcmService.subscribeToJokeTopic();
  }

  unsubscribeFromJokes(): void {
    this.fcmService.unsubscribeFromJokeTopic();
  }

}
