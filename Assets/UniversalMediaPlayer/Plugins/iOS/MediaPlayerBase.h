#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>

NSString * const kUMPErrorDomain = @"kUMPErrorDomain";
float const TIME_CHANGE_OFFSET = 0.265;

enum PlayerStates
{
    Empty,
    Opening,
    Buffering,
    Prepared,
    Playing,
    Paused,
    Stopped,
    EndReached,
    EncounteredError,
    TimeChanged,
    PositionChanged,
    SnapshotTaken
};

@interface PlayerState : NSObject

@property PlayerStates state;
@property float valueFloat;
@property long valueLong;
@property char* valueString;

@end

@interface NSMutableArray (QueueStack)

-(PlayerState*)queuePop;
-(void)queuePush:(PlayerState*)obj;

@end

@protocol MediaPlayerDelegate <NSObject>

@optional
- (void)mediaPlayerStateChanged:(PlayerState*)state;

@end

@protocol MediaPlayerBase

@property (nonatomic, weak) id<MediaPlayerDelegate> delegate;

- (void)setupPlayer:(NSString*)options;
- (void)setDataSource:(NSString*)path;
- (void)play;
- (void)pause;
- (void)stop;
- (void)free;
- (int)duration;
- (CVPixelBufferRef)videoBuffer;
- (int)framesCounter;
- (int)getVolume;
- (void)setVolume:(int)value;
- (int)getTime;
- (void)setTime:(int)value;
- (float)getPosition;
- (void)setPosition:(float)value;
- (float)getPlaybackRate;
- (void)setPlaybackRate:(float)value;
- (bool)isPlaying;
- (bool)isReady;
- (int)getVideoWidth;
- (int)getVideoHeight;

@end
