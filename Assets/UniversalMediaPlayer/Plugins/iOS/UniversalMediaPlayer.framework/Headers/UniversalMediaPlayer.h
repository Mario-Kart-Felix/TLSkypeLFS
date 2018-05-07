/*
 * IJKMediaFramework.h
 *
 * Copyright (c) 2013 Zhang Rui <bbcallen@gmail.com>
 *
 * This file is part of ijkPlayer.
 *
 * ijkPlayer is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * ijkPlayer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with ijkPlayer; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

#import <UIKit/UIKit.h>

//! Project version number for IJKMediaFramework.
FOUNDATION_EXPORT double MediaFrameworkVersionNumber;

//! Project version string for IJKMediaFramework.
FOUNDATION_EXPORT const unsigned char MediaFrameworkVersionString[];

// In this header, you should import all the public headers of your framework using statements like #import <IJKMediaFramework/PublicHeader.h>
#import <UniversalMediaPlayer/MediaPlayback.h>
#import <UniversalMediaPlayer/MMPMoviePlayerController.h>
#import <UniversalMediaPlayer/FFOptions.h>
#import <UniversalMediaPlayer/FFMoviePlayerController.h>
#import <UniversalMediaPlayer/AVMoviePlayerController.h>
#import <UniversalMediaPlayer/MediaModule.h>
#import <UniversalMediaPlayer/MediaPlayer.h>
#import <UniversalMediaPlayer/NotificationManager.h>
#import <UniversalMediaPlayer/KVOController.h>

// backward compatible for old names
#define MediaPlaybackIsPreparedToPlayDidChangeNotification MMPMediaPlaybackIsPreparedToPlayDidChangeNotification
#define MoviePlayerLoadStateDidChangeNotification MMPMoviePlayerLoadStateDidChangeNotification
#define MoviePlayerPlaybackDidFinishNotification MMPMoviePlayerPlaybackDidFinishNotification
#define MoviePlayerPlaybackDidFinishReasonUserInfoKey MMPMoviePlayerPlaybackDidFinishReasonUserInfoKey
#define MoviePlayerPlaybackStateDidChangeNotification MMPMoviePlayerPlaybackStateDidChangeNotification
#define MoviePlayerIsAirPlayVideoActiveDidChangeNotification MMPMoviePlayerIsAirPlayVideoActiveDidChangeNotification
#define MoviePlayerVideoDecoderOpenNotification MMPMoviePlayerVideoDecoderOpenNotification
#define MoviePlayerFirstVideoFrameRenderedNotification MMPMoviePlayerFirstVideoFrameRenderedNotification
#define MoviePlayerFirstAudioFrameRenderedNotification MMPMoviePlayerFirstAudioFrameRenderedNotification

