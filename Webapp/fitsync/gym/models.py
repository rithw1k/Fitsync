from django.db import models
from django.contrib.auth.models import User
from django.core.mail import send_mail
import secrets
import string

class Trainer(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE, null=True, blank=True)
    name = models.CharField(max_length=100)
    email = models.EmailField(unique=True)
    phone = models.CharField(max_length=15, blank=True, null=True)
    specialty = models.CharField(max_length=100, blank=True)
    profile_image = models.ImageField(upload_to='trainers/', blank=True, null=True)
    created_at = models.DateTimeField(auto_now_add=True)
    status = models.BooleanField(default=True)

    def __str__(self):
        return self.name

    def save(self, *args, **kwargs):
        if not self.user:
            username = self.email.split('@')[0]
            alphabet = string.ascii_letters + string.digits + string.punctuation
            password = ''.join(secrets.choice(alphabet) for _ in range(12))
            user = User.objects.create_user(username=username, email=self.email, password=password)
            self.user = user
            send_mail(
                'Your FitSync Trainer Account',
                f'Username: {username}\nPassword: {password}\nLogin at: http://localhost:8000/login/',
                'admin@fitsync.com',
                [self.email],
                fail_silently=False,
            )
        super().save(*args, **kwargs)

class Product(models.Model):
    name = models.CharField(max_length=100)
    description = models.TextField(blank=True)
    price = models.DecimalField(max_digits=6, decimal_places=2)
    stock = models.PositiveIntegerField(default=0)
    image = models.ImageField(upload_to='products/', blank=True, null=True)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return self.name

class Fit_Users(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE, null=True, blank=True)
    username = models.CharField(max_length=100, unique=True)
    email = models.EmailField(unique=True)
    created_at = models.DateTimeField(auto_now_add=True)
    is_active = models.BooleanField(default=True)

    def __str__(self):
        return self.username
class Attendance(models.Model):
    STATUS_CHOICES = [
        ('Present', 'Present'),
        ('Absent', 'Absent'),
    ]

    user = models.ForeignKey(Fit_Users, on_delete=models.CASCADE, related_name='attendance_records')
    date = models.DateField()
    status = models.CharField(max_length=10, choices=STATUS_CHOICES, default='Present')
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.user.username} - {self.date} - {self.status}"

    class Meta:
        unique_together = ('user', 'date')

class Cart(models.Model):
    username = models.CharField(max_length=150)
    product = models.ForeignKey(Product, on_delete=models.CASCADE)
    quantity = models.PositiveIntegerField(default=1)
    added_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.username}'s cart - {self.product.name}"

class Order(models.Model):
    STATUS_CHOICES = [
        ('Pending', 'Pending'),
        ('Delivered', 'Delivered'),
    ]
    
    username = models.CharField(max_length=150)
    products = models.ManyToManyField('Product', through='OrderItem')
    location = models.CharField(max_length=150,default="")
    total_price = models.DecimalField(max_digits=10, decimal_places=2, default=0.00)  # Use Decimal for currency
    added_at = models.DateTimeField(auto_now_add=True)
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='Pending')

    def __str__(self):
        return f"{self.username}'s order"

class OrderItem(models.Model):
    order = models.ForeignKey(Order, on_delete=models.CASCADE)
    product = models.ForeignKey('Product', on_delete=models.CASCADE)
    quantity = models.PositiveIntegerField(default=1)

    def __str__(self):
        return f"{self.quantity} x {self.product.name} in Order {self.order.id}"

class DailyFood(models.Model):
    username = models.CharField(max_length=150)
    date = models.DateField()
    added_details = models.TextField()  
    comments = models.JSONField(default=list)  
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.username}'s food entry on {self.date}"

class Booking(models.Model):
    STATUS_CHOICES = [
        ('Pending', 'Pending'),
        ('Approved', 'Approved'),
    ]

    username = models.CharField(max_length=150)
    trainer_name = models.CharField(max_length=100)
    trainer_email = models.EmailField()
    speciality = models.CharField(max_length=100)
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='Pending')
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.username}'s booking with {self.trainer_name}"



class TrainerVideo(models.Model):
    trainer_email = models.EmailField()
    details = models.TextField()
    video = models.FileField(upload_to='trainer_videos/')  # Videos will be stored in media/trainer_videos/
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"Video by {self.trainer_email} - {self.created_at.strftime('%Y-%m-%d %H:%M')}"


class Reviews(models.Model):
    username = models.CharField(max_length=150)
    trainer_email = models.EmailField()
    rating = models.IntegerField()  # Rating out of 5 (1-5)
    comment = models.TextField()
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"Review by {self.username} for {self.trainer_email}"

    class Meta:
        unique_together = ('username', 'trainer_email')