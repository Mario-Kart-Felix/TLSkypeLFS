#include "MediaPlayerNative.h"

static void *VideoPlayer_PlayerItemStatusContext = &VideoPlayer_PlayerItemStatusContext;
static void *VideoPlayer_PlayerRateChangedContext = &VideoPlayer_PlayerRateChangedContext;
static void *VideoPlayer_PlayerItemPlaybackBufferEmpty = &VideoPlayer_PlayerItemPlaybackBufferEmpty;
static void *VideoPlayer_PlayerItemLoadedTimeRangesContext = &VideoPlayer_PlayerItemLoadedTimeRangesContext;

@implementation MediaPlayerNative

@synthesize delegate = _delegate;
@synthesize framesCounter = _framesCounter;

- (void)dealloc
{
    [self free];
}

- (id)init
{
    self = [super init];
    _instance = self;
    return _instance;
}

- (void)setupPlayer:(NSString *)options
{
    if (self)
    {
        _player = [[AVPlayer alloc] init];
        [self addPlayerObservers];
    }
}

- (void)setDataSource:(NSString *)path
{
    _url = [NSURL URLWithString:path];
    
    if (_url == nil)
        return;
    
    [self resetPlayerItemIfNecessary];
    
    AVPlayerItem *playerItem = [[AVPlayerItem alloc] initWithURL:_url];
    
    if (!playerItem)
    {
        [self reportUnableToCreatePlayerItem];
        return;
    }
    
    [self preparePlayerItem:playerItem];
}

- (void)play
{
    if (self.player.currentItem == nil)
    {
        if (_url == nil)
            return;
        
        [self resetPlayerItemIfNecessary];
        
        AVPlayerItem *playerItem = [[AVPlayerItem alloc] initWithURL:_url];
        
        if (!playerItem)
        {
            [self reportUnableToCreatePlayerItem];
            return;
        }
        
        [self preparePlayerItem:playerItem];
    }
    
    self.playing = YES;
    
    if (_ready)
        [self.player play];
}

- (void)pause
{
    self.playing = NO;
    
    [self.player pause];
}

- (void)stop
{
    [self pause];
    [self resetPlayerItemIfNecessary];
    
    if(_itemVideoOutput)
        _itemVideoOutput = nil;
    
    _framesCounter = 0;
    
    if ([_delegate respondsToSelector:@selector(mediaPlayerStateChanged:)])
    {
        PlayerState *newState = [[PlayerState alloc] init];
        newState.state = Stopped;
        [_delegate mediaPlayerStateChanged:newState];
    }
}

- (void)free
{
    [self stop];
    [self removePlayerObservers];
    [self cleanupTextureBuffer];
}

- (int)duration
{
    return _duration * 1000;
}

- (CVPixelBufferRef)videoBuffer
{
    if (self.isPlaying)
    {
        float currentTime = CMTimeGetSeconds([_player currentTime]);
        if (fabs(currentTime - _cachedPlaybackTime) > TIME_CHANGE_OFFSET)
        {
            if ([_delegate respondsToSelector:@selector(mediaPlayerStateChanged:)])
            {
                dispatch_async(dispatch_get_main_queue(), ^{
                    PlayerState *newState = [[PlayerState alloc] init];
                    newState.state = TimeChanged;
                    newState.valueLong = currentTime * 1000;
                    [_delegate mediaPlayerStateChanged:newState];
                });
            }
        
            if ([_delegate respondsToSelector:@selector(mediaPlayerStateChanged:)])
            {
                dispatch_async(dispatch_get_main_queue(), ^{
                    PlayerState *newState = [[PlayerState alloc] init];
                    newState.state = PositionChanged;
                    newState.valueFloat = currentTime / _duration;
                    [_delegate mediaPlayerStateChanged:newState];
                });
            }
        }
    }
    
    if(_framesCounter != _cachedFramesCounter)
    {
        if (_textureBuffer)
            CVPixelBufferRelease(_textureBuffer);
        
        _textureBuffer = [_itemVideoOutput copyPixelBufferForItemTime:_player.currentItem.currentTime itemTimeForDisplay:nil];

        [self bufferFlipVertically:_textureBuffer];
        
        if (_textureBuffer == nil)
            return nil;
        
        int width = (int)CVPixelBufferGetWidth(_textureBuffer);
        int height = (int)CVPixelBufferGetHeight(_textureBuffer);

        if (_videoSize.width != width || _videoSize.height != height)
            _videoSize = CGSizeMake(width, height);

        _cachedFramesCounter = _framesCounter;
        return _textureBuffer;
    }
    
    return nil;
}

- (void)releaseBuffer
{
    if (_textureBuffer)
    {
        CVPixelBufferRelease(_textureBuffer);
        _textureBuffer = nil;
    }
}

- (int)framesCounter
{
    if (_ready)
    {
        CMTime outputItemTime = [_itemVideoOutput itemTimeForHostTime:CACurrentMediaTime()];
    
        if([_itemVideoOutput hasNewPixelBufferForItemTime:outputItemTime])
            _framesCounter++;
    }
    
    return _framesCounter;
}

- (int)getVolume
{
    if (_ready)
        return self.player.volume * 100;
    
    return 0;
}

- (void)setVolume:(int)value
{
    if (_ready)
        self.player.volume = (float)value / 100.0;
}

- (int)getTime
{
    CMTime time = kCMTimeZero;
    if (_ready)
        time = [_player currentTime];
    
    return CMTIME_IS_VALID(time) ? (int)(CMTimeGetSeconds(time) * 1000) : 0;
}

- (void)setTime:(int)value
{
    if (_seeking || !_ready)
        return;
    
    if (self.player)
    {
        float time = (float)value / 1000.0;
        CMTime cmTime = CMTimeMakeWithSeconds(time, self.player.currentTime.timescale);
        
        _seeking = YES;
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            [self.player seekToTime:cmTime completionHandler:^(BOOL finished)
            {
                _seeking = NO;
            }];
        });
    }
}

- (float)getPosition
{
    CMTime time = kCMTimeZero;
    if (_ready)
        time = [_player currentTime];
    
    return CMTIME_IS_VALID(time) ? CMTimeGetSeconds(time) / _duration : 0;
}

- (void)setPosition:(float)value
{
    if (_ready)
        [self setTime:((_duration * value) * 1000)];
}

- (float)getPlaybackRate
{
    return _player.rate;
}

- (void)setPlaybackRate:(float)value
{
    [_player setRate:value];
}

- (int)getVideoWidth
{
    return _videoSize.width;
}

- (int)getVideoHeight
{
    return _videoSize.height;
}

- (BOOL)isPlaying
{
    return self.playing;
}

- (BOOL)isReady
{
    return self.ready;
}

- (void)cleanupTextureBuffer
{
    if(_textureBuffer)
    {
        CFRelease(_textureBuffer);
        _textureBuffer = 0;
    }
}

- (void)reportUnableToCreatePlayerItem
{
    NSLog(@"Unable to create AVPlayerItem.");
    
    if ([_delegate respondsToSelector:@selector(mediaPlayerStateChanged:)])
    {
        dispatch_async(dispatch_get_main_queue(), ^{
            PlayerState *newState = [[PlayerState alloc] init];
            newState.state = EncounteredError;
            [_delegate mediaPlayerStateChanged:newState];
        });
    }
}

- (void)resetPlayerItemIfNecessary
{
    if (self.playerItem)
    {
        [self removePlayerItemObservers:self.playerItem];
        
        [self.player replaceCurrentItemWithPlayerItem:nil];
        
        self.playerItem = nil;
    }
    
    _playing = NO;
    _ready = NO;
}

- (void)preparePlayerItem:(AVPlayerItem *)playerItem
{
    NSParameterAssert(playerItem);
    self.playerItem = playerItem;
    
    [self addPlayerItemObservers:playerItem];
    
    [self.player replaceCurrentItemWithPlayerItem:playerItem];
}

- (void)restart
{
    [self.player seekToTime:kCMTimeZero toleranceBefore:kCMTimeZero toleranceAfter:kCMTimeZero completionHandler:^(BOOL finished) {
        
        if (finished)
        {
            if (self.isPlaying)
            {
                [self play];
            }
        }
        
    }];
}

- (float)getLoadedDuration
{
    float loadedDuration = 0.0f;
    
    if (self.player && self.player.currentItem)
    {
        NSArray *loadedTimeRanges = self.player.currentItem.loadedTimeRanges;
        
        if (loadedTimeRanges && [loadedTimeRanges count])
        {
            CMTimeRange timeRange = [[loadedTimeRanges firstObject] CMTimeRangeValue];
            loadedDuration = CMTimeGetSeconds(CMTimeAdd(timeRange.start, timeRange.duration));
        }
    }
    
    return loadedDuration;
}

- (void)bufferFlipVertically:(CVPixelBufferRef)buffer
{
    @autoreleasepool
    {
        if (_context==nil)
        {
            _rgbColorSpace = CGColorSpaceCreateDeviceRGB();
            _context = [CIContext contextWithOptions:@{kCIContextWorkingColorSpace: (__bridge id)_rgbColorSpace,
                                                       kCIContextOutputColorSpace : (__bridge id)_rgbColorSpace}];
        }
        
        long int w = CVPixelBufferGetWidth(buffer);
        long int h = CVPixelBufferGetHeight(buffer);
        
        CIImage* flipedImage = [CIImage imageWithCVPixelBuffer:buffer];
        
        double horizontalSpace = fabs(w*cos(0)) + fabs(h*sin(0));
        double scalingFact = (double)w / horizontalSpace;
        
        CGAffineTransform transform =  CGAffineTransformMakeTranslation(w, h);
        transform = CGAffineTransformScale(transform, 1.0, -1.0);
        
        flipedImage = [flipedImage imageByApplyingTransform:transform];
        
        CVPixelBufferLockBaseAddress(buffer, 0);
        
        CGRect extentR = [flipedImage extent];
        CGPoint centerP = CGPointMake(extentR.size.width/2.0+extentR.origin.x,
                                      extentR.size.height/2.0+extentR.origin.y);
        CGSize scaledSize = CGSizeMake(w*scalingFact, h*scalingFact);
        CGRect cropRect = CGRectMake(centerP.x-scaledSize.width/2.0, centerP.y-scaledSize.height/2.0,
                                     scaledSize.width, scaledSize.height);
        
        
        CGImageRef cg_img = [_context createCGImage:flipedImage fromRect:cropRect];
        flipedImage = [CIImage imageWithCGImage:cg_img];
        
        flipedImage = [flipedImage imageByApplyingTransform:CGAffineTransformMakeScale(1.0/scalingFact, 1.0/scalingFact)];
        
        [_context render:flipedImage toCVPixelBuffer:buffer bounds:CGRectMake(0, 0, w, h) colorSpace:NULL];
        
        CGImageRelease(cg_img);
        CVPixelBufferUnlockBaseAddress(buffer, 0);
    }
}

- (void)addPlayerObservers
{
    [self.player addObserver:self
                  forKeyPath:NSStringFromSelector(@selector(rate))
                     options:NSKeyValueObservingOptionInitial | NSKeyValueObservingOptionNew
                     context:VideoPlayer_PlayerRateChangedContext];
}

- (void)removePlayerObservers
{
    [self.player removeObserver:self
                     forKeyPath:NSStringFromSelector(@selector(rate))
                        context:VideoPlayer_PlayerRateChangedContext];
}

- (void)addPlayerItemObservers:(AVPlayerItem *)playerItem
{
    [playerItem addObserver:self
                 forKeyPath:NSStringFromSelector(@selector(status))
                    options:NSKeyValueObservingOptionInitial | NSKeyValueObservingOptionOld | NSKeyValueObservingOptionNew
                    context:VideoPlayer_PlayerItemStatusContext];
    
    [playerItem addObserver:self
                 forKeyPath:NSStringFromSelector(@selector(isPlaybackBufferEmpty))
                    options:NSKeyValueObservingOptionInitial | NSKeyValueObservingOptionNew
                    context:VideoPlayer_PlayerItemPlaybackBufferEmpty];
    
    [playerItem addObserver:self
                 forKeyPath:NSStringFromSelector(@selector(loadedTimeRanges))
                    options:NSKeyValueObservingOptionInitial | NSKeyValueObservingOptionNew
                    context:VideoPlayer_PlayerItemLoadedTimeRangesContext];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(playerItemDidPlayToEndTime:)
                                                 name:AVPlayerItemDidPlayToEndTimeNotification
                                               object:playerItem];
}

- (void)removePlayerItemObservers:(AVPlayerItem *)playerItem
{
    [playerItem cancelPendingSeeks];
    
    [playerItem removeObserver:self
                    forKeyPath:NSStringFromSelector(@selector(status))
                       context:VideoPlayer_PlayerItemStatusContext];
    
    [playerItem removeObserver:self
                    forKeyPath:NSStringFromSelector(@selector(isPlaybackBufferEmpty))
                       context:VideoPlayer_PlayerItemPlaybackBufferEmpty];
    
    [playerItem removeObserver:self
                    forKeyPath:NSStringFromSelector(@selector(loadedTimeRanges))
                       context:VideoPlayer_PlayerItemLoadedTimeRangesContext];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self name:AVPlayerItemDidPlayToEndTimeNotification object:playerItem];
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
    if (context == VideoPlayer_PlayerRateChangedContext)
    {
    }
    else if (context == VideoPlayer_PlayerItemStatusContext)
    {
        AVPlayerStatus newStatus = (AVPlayerStatus)[[change objectForKey:NSKeyValueChangeNewKey] integerValue];
        AVPlayerStatus oldStatus = (AVPlayerStatus)[[change objectForKey:NSKeyValueChangeOldKey] integerValue];
        
        if (newStatus != oldStatus)
        {
            switch (newStatus)
            {
                case AVPlayerItemStatusUnknown:
                {
                    NSLog(@"Video player Status Unknown");
                    break;
                }
                case AVPlayerItemStatusReadyToPlay:
                {
                    if (_itemVideoOutput == nil)
                    {
                        NSDictionary *options = @{ (__bridge NSString *)kCVPixelBufferPixelFormatTypeKey : @(kCVPixelFormatType_32BGRA),
                                                   (__bridge NSString *)kCVPixelBufferOpenGLESCompatibilityKey : @YES };
                        
                        _itemVideoOutput = [[AVPlayerItemVideoOutput alloc] initWithPixelBufferAttributes:options];
                        [[_player currentItem] addOutput:_itemVideoOutput];
                        
                        _ready = YES;
                        
                        _duration = 0;
                        if (CMTIME_IS_INVALID(self.player.currentItem.duration) == NO)
                            _duration = CMTimeGetSeconds(self.player.currentItem.duration);
                        
                        if ([_delegate respondsToSelector:@selector(mediaPlayerStateChanged:)])
                        {
                            dispatch_async(dispatch_get_main_queue(), ^{
                                PlayerState *newState = [[PlayerState alloc] init];
                                newState.state = Playing;
                                [_delegate mediaPlayerStateChanged:newState];
                            });
                        }
                    }
                    
                    if (self.isPlaying)
                        [self play];
                    
                    break;
                }
                case AVPlayerItemStatusFailed:
                {
                    NSLog(@"Video player Status Failed: player item error = %@", self.player.currentItem.error);
                    NSLog(@"Video player Status Failed: player error = %@", self.player.error);
                    
                    [self stop];
                    
                    if ([_delegate respondsToSelector:@selector(mediaPlayerStateChanged:)])
                    {
                        dispatch_async(dispatch_get_main_queue(), ^{
                            PlayerState *newState = [[PlayerState alloc] init];
                            newState.state = EncounteredError;
                            [_delegate mediaPlayerStateChanged:newState];
                        });
                    }
                    
                    break;
                }
            }
        }
    }
    else if (context == VideoPlayer_PlayerItemPlaybackBufferEmpty)
    {
        if ([_delegate respondsToSelector:@selector(mediaPlayerStateChanged:)])
        {
            dispatch_async(dispatch_get_main_queue(), ^{
                PlayerState *newState = [[PlayerState alloc] init];
                newState.state = Opening;
                [_delegate mediaPlayerStateChanged:newState];
            });
        }
    }
    else if (context == VideoPlayer_PlayerItemLoadedTimeRangesContext)
    {
        float loadedDuration = [self getLoadedDuration];

        if ([_delegate respondsToSelector:@selector(mediaPlayerStateChanged:)])
        {
            dispatch_async(dispatch_get_main_queue(), ^{
                PlayerState *newState = [[PlayerState alloc] init];
                newState.state = Buffering;
                newState.valueFloat = loadedDuration;
                [_delegate mediaPlayerStateChanged:newState];
            });
        }
    }
    else
    {
        [super observeValueForKeyPath:keyPath ofObject:object change:change context:context];
    }
}

- (void)playerItemDidPlayToEndTime:(NSNotification *)notification
{
    if (notification.object != self.player.currentItem)
        return;
    
    self.playing = NO;
    
    if ([_delegate respondsToSelector:@selector(mediaPlayerStateChanged:)])
    {
        dispatch_async(dispatch_get_main_queue(), ^{
            PlayerState *newState = [[PlayerState alloc] init];
            newState.state = EndReached;
            [_delegate mediaPlayerStateChanged:newState];
        });
    }
}

@end
