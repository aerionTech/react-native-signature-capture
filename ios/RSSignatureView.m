#import "RSSignatureView.h"
#import <React/RCTConvert.h>
#import <UIKit/UIKit.h>
#import <QuartzCore/QuartzCore.h>
#import "PPSSignatureView.h"
#import "RSSignatureViewManager.h"

#define DEGREES_TO_RADIANS(x) (M_PI * (x) / 180.0)

@implementation RSSignatureView {
	CAShapeLayer *_border;
	BOOL _loaded;
	EAGLContext *_context;
	UIButton *saveButton;
	UIButton *clearButton;
	UILabel *titleLabel;
	BOOL _rotateClockwise;
	BOOL _square;
	BOOL _showBorder;
	BOOL _showNativeButtons;
	BOOL _showTitleLabel;
    int _maxSize;
}

@synthesize sign;
@synthesize manager;

- (instancetype)init
{
  _showBorder = YES;
	_showNativeButtons = YES;
	_showTitleLabel = YES;
    _maxSize = 500;
	if ((self = [super init])) {
		_border = [CAShapeLayer layer];
		_border.strokeColor = [UIColor blackColor].CGColor;
		_border.fillColor = nil;
		_border.lineDashPattern = @[@4, @2];

		[self.layer addSublayer:_border];
	}

	return self;
}

- (void) didRotate:(NSNotification *)notification {
	int ori=1;
	UIDeviceOrientation currOri = [[UIDevice currentDevice] orientation];
	if ((currOri == UIDeviceOrientationLandscapeLeft) || (currOri == UIDeviceOrientationLandscapeRight)) {
		ori=0;
	}
}

- (void)layoutSubviews
{
	[super layoutSubviews];
	if (!_loaded) {

		[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didRotate:)
																								 name:UIDeviceOrientationDidChangeNotification object:nil];

		_context = [[EAGLContext alloc] initWithAPI:kEAGLRenderingAPIOpenGLES2];

		CGSize screen = self.bounds.size;
        
        
        UIImageView *bgImageView = [[UIImageView alloc] initWithImage:[self getBackgroundImageWithSize:screen]];
        bgImageView.frame = CGRectMake(0, 0, screen.width, screen.height);
        [self addSubview:bgImageView];
        [self sendSubviewToBack:bgImageView];

		sign = [[PPSSignatureView alloc]
						initWithFrame: CGRectMake(0, 0, screen.width, screen.height)
						context: _context];
		sign.manager = manager;

		[self addSubview:sign];

		if ( UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ) {

			if (_showTitleLabel) {
				titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, self.bounds.size.width, 24)];
				[titleLabel setCenter:CGPointMake(self.bounds.size.width/2, self.bounds.size.height - 120)];

				[titleLabel setText:@"x_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _"];
				[titleLabel setLineBreakMode:NSLineBreakByClipping];
				[titleLabel setTextAlignment: NSTextAlignmentCenter];
				[titleLabel setTextColor:[UIColor colorWithRed:200/255.f green:200/255.f blue:200/255.f alpha:1.f]];
				//[titleLabel setBackgroundColor:[UIColor greenColor]];
				[sign addSubview:titleLabel];
			}

			if (_showNativeButtons) {
				//Save button
				saveButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
				[saveButton setLineBreakMode:NSLineBreakByClipping];
				[saveButton addTarget:self action:@selector(onSaveButtonPressed)
				            forControlEvents:UIControlEventTouchUpInside];
				[saveButton setTitle:@"Save" forState:UIControlStateNormal];

				CGSize buttonSize = CGSizeMake(80, 55.0);

				saveButton.frame = CGRectMake(sign.bounds.size.width - buttonSize.width,
				                              0, buttonSize.width, buttonSize.height);
				[saveButton setBackgroundColor:[UIColor colorWithRed:250/255.f green:250/255.f blue:250/255.f alpha:1.f]];
				[sign addSubview:saveButton];


				//Clear button
				clearButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
				[clearButton setLineBreakMode:NSLineBreakByClipping];
				[clearButton addTarget:self action:@selector(onClearButtonPressed)
				             forControlEvents:UIControlEventTouchUpInside];
				[clearButton setTitle:@"Reset" forState:UIControlStateNormal];

				clearButton.frame = CGRectMake(0, 0, buttonSize.width, buttonSize.height);
				[clearButton setBackgroundColor:[UIColor colorWithRed:250/255.f green:250/255.f blue:250/255.f alpha:1.f]];
				[sign addSubview:clearButton];
			}
		}
		else {

			if (_showTitleLabel) {
				titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, self.bounds.size.height - 80, 24)];
				[titleLabel setCenter:CGPointMake(40, self.bounds.size.height/2)];
				[titleLabel setTransform:CGAffineTransformMakeRotation(DEGREES_TO_RADIANS(90))];
				[titleLabel setText:@"x_ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _ _"];
				[titleLabel setLineBreakMode:NSLineBreakByClipping];
				[titleLabel setTextAlignment: NSTextAlignmentLeft];
				[titleLabel setTextColor:[UIColor colorWithRed:200/255.f green:200/255.f blue:200/255.f alpha:1.f]];
				//[titleLabel setBackgroundColor:[UIColor greenColor]];
				[sign addSubview:titleLabel];
			}

			if (_showNativeButtons) {
				//Save button
				saveButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
				[saveButton setTransform:CGAffineTransformMakeRotation(DEGREES_TO_RADIANS(90))];
				[saveButton setLineBreakMode:NSLineBreakByClipping];
				[saveButton addTarget:self action:@selector(onSaveButtonPressed)
				            forControlEvents:UIControlEventTouchUpInside];
				[saveButton setTitle:@"Save" forState:UIControlStateNormal];

				CGSize buttonSize = CGSizeMake(55, 80.0); //Width/Height is swapped

				saveButton.frame = CGRectMake(sign.bounds.size.width - buttonSize.width, sign.bounds.size.height - buttonSize.height, buttonSize.width, buttonSize.height);
				[saveButton setBackgroundColor:[UIColor colorWithRed:250/255.f green:250/255.f blue:250/255.f alpha:1.f]];
				[sign addSubview:saveButton];

				//Clear button
				clearButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
				[clearButton setTransform:CGAffineTransformMakeRotation(DEGREES_TO_RADIANS(90))];
				[clearButton setLineBreakMode:NSLineBreakByClipping];
				[clearButton addTarget:self action:@selector(onClearButtonPressed)
				             forControlEvents:UIControlEventTouchUpInside];
				[clearButton setTitle:@"Reset" forState:UIControlStateNormal];

				clearButton.frame = CGRectMake(sign.bounds.size.width - buttonSize.width, 0, buttonSize.width, buttonSize.height);
				[clearButton setBackgroundColor:[UIColor colorWithRed:250/255.f green:250/255.f blue:250/255.f alpha:1.f]];
				[sign addSubview:clearButton];
			}
		}

	}
	_loaded = true;
	_border.path = _showBorder ? [UIBezierPath bezierPathWithRect:self.bounds].CGPath : nil;
	_border.frame = self.bounds;
}

- (void)setRotateClockwise:(BOOL)rotateClockwise {
	_rotateClockwise = rotateClockwise;
}

- (void)setSquare:(BOOL)square {
	_square = square;
}

- (void)setShowBorder:(BOOL)showBorder {
	_showBorder = showBorder;
}

- (void)setShowNativeButtons:(BOOL)showNativeButtons {
	_showNativeButtons = showNativeButtons;
}

- (void)setShowTitleLabel:(BOOL)showTitleLabel {
	_showTitleLabel = showTitleLabel;
}

- (void)setMaxSize:(int)maxSize {
    _maxSize = maxSize;
}

-(void) onSaveButtonPressed {
	[self saveImageWithWatermark:nil];
}

-(void) saveImageWithWatermark:(NSString*)watermark {
	saveButton.hidden = YES;
	clearButton.hidden = YES;
    UIImage *signImage = [self.sign signatureImage: _rotateClockwise withSquare:_square withMaxSize:_maxSize withBackground:[self getBackgroundImageWithSize:self.bounds.size] withWatermark:watermark];

	saveButton.hidden = NO;
	clearButton.hidden = NO;

	NSError *error;

	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDirectory = [paths firstObject];
	NSString *tempPath = [documentsDirectory stringByAppendingFormat:@"/signature.png"];

	//remove if file already exists
	if ([[NSFileManager defaultManager] fileExistsAtPath:tempPath]) {
		[[NSFileManager defaultManager] removeItemAtPath:tempPath error:&error];
		if (error) {
			NSLog(@"Error: %@", error.debugDescription);
		}
	}

	// Convert UIImage object into NSData (a wrapper for a stream of bytes) formatted according to PNG spec
	NSData *imageData = UIImagePNGRepresentation(signImage);
	BOOL isSuccess = [imageData writeToFile:tempPath atomically:YES];
	if (isSuccess) {
		NSFileManager *man = [NSFileManager defaultManager];
		NSDictionary *attrs = [man attributesOfItemAtPath:tempPath error: NULL];
		//UInt32 result = [attrs fileSize];

		NSString *base64Encoded = [imageData base64EncodedStringWithOptions:0];
		[self.manager publishSaveImageEvent: tempPath withEncoded:base64Encoded];
	}
}

-(void) onClearButtonPressed {
	[self erase];
}

-(void) erase {
	[self.sign erase];
}


- (UIImage*) getBackgroundImageWithSize:(CGSize) size {
    NSString* _encodedImage = @"/9j/4QAYRXhpZgAASUkqAAgAAAAAAAAAAAAAAP/sABFEdWNreQABAAQAAABkAAD/4QMraHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLwA8P3hwYWNrZXQgYmVnaW49Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/PiA8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4OnhtcHRrPSJBZG9iZSBYTVAgQ29yZSA1LjMtYzAxMSA2Ni4xNDU2NjEsIDIwMTIvMDIvMDYtMTQ6NTY6MjcgICAgICAgICI+IDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+IDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bXA6Q3JlYXRvclRvb2w9IkFkb2JlIFBob3Rvc2hvcCBDUzYgKFdpbmRvd3MpIiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOjI0RTVFNjI4NzYwMjExRTk4QkQzQ0E0OUMyNTVGRDVFIiB4bXBNTTpEb2N1bWVudElEPSJ4bXAuZGlkOjI0RTVFNjI5NzYwMjExRTk4QkQzQ0E0OUMyNTVGRDVFIj4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6MjRFNUU2MjY3NjAyMTFFOThCRDNDQTQ5QzI1NUZENUUiIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6MjRFNUU2Mjc3NjAyMTFFOThCRDNDQTQ5QzI1NUZENUUiLz4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz7/7gAOQWRvYmUAZMAAAAAB/9sAhAABAQEBAQEBAQEBAQEBAQEBAQEB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GHRXHJ1DD4EGB5rp9Oh6kopQ81qRT8QoMD2gICAgfCobYwBHiAftgfFRF2qiqf3VA+wQOoCAgICAgICAgIFb3ZsaG0fmatf/AMit+yB59mTp0hfjZaxz4KAoHoQYFtAQEBgbDjaNx5Z5QEBAQEBAQEBAQEBAQEBAQEBAQPhAYEMAQdhBGQfMHYYEY6LSE9R01Oc5+4APVQOkwJIAAAAAA2AAYAHIAboH2AgICAgIDA2nG07zzxzgICAgICAgIELuP+y1H6P8SwInZB/+rYed7fAV1wLiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgVXeP8AZ/8Ayp/igevaxjQ0ePuH42vAsICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgZbuTvXeGTXNaQdiKxDVHl/LAr+w+ECM/ctZZWKzcQMYLKArt+plAPwxnjAu+1Y9vP8AWG9iBmnqJFXo49zPlhfPfAt4CAgICAgQe5HGh1B/dUfGxB+2BH7N/tD/AO8//KkC2gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIFT3o40ij81yD+67fsgSO2/wCx0/6W/wDseBOgICAgICAgICAgICAgICAgICAgICAgICAgICAgIEfVaf8AqaWq9x687mQkejDZ1KeIgUun7K3VnUuvSDsSsklwOJYgdIPx8oFld2zSW1hFrFRUYV69jD9Wc9frt8YEHS9naq73LbQVQ5QVllZjzY7Cg8AdvOBfQEBAQEBAr+6DOhv8PbPwtSB49m/2Z/8Aef8A5UgW0BAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAp+9DOlQ/lvXPkUsH2wJXbf9jp/wBLf/Y8CdAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQECNrE69LqF502EeaqWX5iBXdkbOntTit3V6OigfNDAuoCAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBX90Xq0N3Nehh6Ouf7pMDntL9WirHFGsQ/2yw+TCBZQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAEAgg7QdhHgYEPS6KrR+77Rc+6ykhiD0hc9KjYNg6j4wJkBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBA5dFsRkcZV1KsOYIwRA8dNpq9LWa6urpLlyWIJJIA3gAbAAIEiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICB/9k=";
    
    NSData *bgData = [[NSData alloc]initWithBase64EncodedString:_encodedImage options:NSDataBase64DecodingIgnoreUnknownCharacters];
    UIImage *bgImage = [UIImage imageWithData:bgData];
    
    UIGraphicsBeginImageContext(size);
    [bgImage drawInRect:CGRectMake(0, 0, size.width, size.height)];
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return image;
}

@end
