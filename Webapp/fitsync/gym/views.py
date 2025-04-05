from django.shortcuts import render, redirect
from django.contrib.auth import authenticate, login, logout
from django.contrib.auth.decorators import login_required
from django.contrib.auth.models import User
from django.contrib import messages
from django.http import JsonResponse
from django.core.mail import send_mail
from .models import *
from django.views.decorators.csrf import csrf_exempt
import secrets
import string
import logging 
from datetime import datetime
from django.db.models import Q

# Set up logging
logger = logging.getLogger(__name__)

def login_view(request):
    if request.method == "POST":
        username = request.POST['username'].strip()
        password = request.POST['password']

        print("Trying to authenticate:", username)
        
        user = authenticate(request, username=username, password=password)
        print("Authenticated User:", user)

        if user is not None:
            login(request, user)
            if user.is_superuser and username.lower() == 'admin':
                return JsonResponse({'success': True, 'redirect_url': '/dashboard/'})
            else:
                try:
                    trainer = Trainer.objects.get(user=user)
                    request.session['trainer_id'] = trainer.id
                    request.session['trainer_name'] = trainer.name
                    request.session['trainer_email'] = trainer.email
                    request.session['trainer_phone'] = trainer.phone
                    request.session['trainer_specialty'] = trainer.specialty
                    return JsonResponse({'success': True, 'redirect_url': '/trainer_dashboard/'})
                except Trainer.DoesNotExist:
                    return JsonResponse({'success': False, 'error': 'Not authorized as Admin or Trainer'})
        else:
            return JsonResponse({'success': False, 'error': 'Invalid credentials'})

    return render(request, 'login.html')


def dashboard_view(request):
    if not request.user.is_authenticated:
        return redirect('login')
    return render(request, 'dashboard.html')

def trainer_dashboard_view(request):
    if not request.user.is_authenticated:
        return redirect('login')
    try:
        trainer = Trainer.objects.get(user=request.user)
    except Trainer.DoesNotExist:
        return redirect('login')
    context = {
        'trainer': trainer
    }
    return render(request, 'trainer_dashboard.html', context)

def logout_view(request):
    logout(request)
    request.session.flush()
    return redirect('login')

def add_trainer_view(request):
    if not request.user.is_authenticated or not request.user.is_superuser:
        return redirect('login')
    
    if request.method == "POST":
        name = request.POST.get('name')
        email = request.POST.get('email')
        phone = request.POST.get('phone')
        specialty = request.POST.get('specialty')
        profile_image = request.FILES.get('profile_image')
        
        try:
            trainer = Trainer(
                name=name,
                email=email,
                phone=phone,
                specialty=specialty,
                profile_image=profile_image
            )
            trainer.save()
            return JsonResponse({
                'success': True,
                'message': 'Trainer added successfully!',
                'redirect_url': '/dashboard/'
            })
        except Exception as e:
            return JsonResponse({
                'success': False,
                'error': f'Error adding trainer: {str(e)}'
            })
    
    return render(request, 'add_trainer.html')

@login_required
def manage_trainers_view(request):
    if not request.user.is_superuser:
        return redirect('login')

    trainers = Trainer.objects.all()

    if request.method == "POST":
        action = request.POST.get('action')
        trainer_id = request.POST.get('trainer_id')

        try:
            trainer = Trainer.objects.get(id=trainer_id)
            if action == 'delete':
                user = trainer.user  
                trainer.delete()
                if user:
                    user.delete()  
                return JsonResponse({'success': True, 'message': 'Trainer and associated user deleted successfully!'})
            elif action == 'edit':
                old_email = trainer.email
                trainer.name = request.POST.get('name')
                new_email = request.POST.get('email')
                trainer.email = new_email
                trainer.phone = request.POST.get('phone')
                trainer.specialty = request.POST.get('specialty')
                trainer.status = request.POST.get('status') == 'on'
                if 'profile_image' in request.FILES:
                    trainer.profile_image = request.FILES['profile_image']

                if old_email != new_email and trainer.user:
                    username = new_email.split('@')[0]
                    trainer.user.username = username
                    trainer.user.email = new_email
                    trainer.user.save()

                trainer.save()
                return JsonResponse({'success': True, 'message': 'Trainer updated successfully!'})
        except Exception as e:
            return JsonResponse({'success': False, 'error': str(e)})

    context = {'trainers': trainers}
    return render(request, 'manage_trainers.html', context)

def add_product_view(request):
    if not request.user.is_authenticated or not request.user.is_superuser:
        return redirect('login')
    
    if request.method == "POST":
        name = request.POST.get('name')
        description = request.POST.get('description')
        price = request.POST.get('price')
        stock = request.POST.get('stock')
        image = request.FILES.get('image')
        
        try:
            product = Product(
                name=name,
                description=description,
                price=price,
                stock=stock,
                image=image
            )
            product.save()
            return JsonResponse({
                'success': True,
                'message': 'Product added successfully!',
                'redirect_url': '/dashboard/'
            })
        except Exception as e:
            return JsonResponse({
                'success': False,
                'error': f'Error adding product: {str(e)}'
            })
    
    return render(request, 'add_product.html')

def manage_products_view(request):
    if not request.user.is_authenticated or not request.user.is_superuser:
        return redirect('login')
    
    products = Product.objects.all()
    
    if request.method == "POST":
        action = request.POST.get('action')
        product_id = request.POST.get('product_id')
        
        try:
            product = Product.objects.get(id=product_id)
            if action == 'delete':
                product.delete()
                return JsonResponse({'success': True, 'message': 'Product deleted successfully!'})
            elif action == 'edit':
                product.name = request.POST.get('name')
                product.description = request.POST.get('description')
                product.price = request.POST.get('price')
                product.stock = request.POST.get('stock')
                if 'image' in request.FILES:
                    product.image = request.FILES['image']
                product.save()
                return JsonResponse({'success': True, 'message': 'Product updated successfully!'})
        except Exception as e:
            return JsonResponse({'success': False, 'error': str(e)})
    
    context = {'products': products}
    return render(request, 'manage_products.html', context)
@csrf_exempt
def api_signup_view(request):
    if request.method == "POST":
        try:
            # Log the incoming POST data
            logger.debug("Received POST data: %s", request.POST)

            username = request.POST.get('username')
            email = request.POST.get('email')
            password = request.POST.get('password')

            # Log the extracted values
            logger.debug("Username: %s, Email: %s, Password: %s", username, email, password)

            # Validate inputs
            if not username or not email or not password:
                return JsonResponse({'success': False, 'error': 'All fields are required'})

            # Check if username or email already exists
            if Fit_Users.objects.filter(username=username).exists():
                logger.warning("Signup failed: Username %s already exists", username)
                return JsonResponse({'success': False, 'error': 'Username already exists'})
            if Fit_Users.objects.filter(email=email).exists():
                logger.warning("Signup failed: Email %s already exists", email)
                return JsonResponse({'success': False, 'error': 'Email already exists'})

            user = User.objects.create_user(username=username, email=email, password=password)
            fit_user = Fit_Users(username=username, email=email, user=user)
            fit_user.save()

            send_mail(
                'Welcome to FitSync!',
                f'Your account has been created.\nUsername: {username}\nPassword: {password}\nLogin at: http://localhost:8000/login/',
                'admin@fitsync.com',
                [email],
                fail_silently=False,
            )

            logger.info("Signup successful for username: %s", username)
            return JsonResponse({'success': True, 'message': 'Signup successful'})
        except Exception as e:
            logger.error("Signup error: %s", str(e))
            return JsonResponse({'success': False, 'error': str(e)})
    return JsonResponse({'success': False, 'error': 'Invalid request'})

@csrf_exempt
def api_login_view(request):
    if request.method == "POST":
        try:
            # Log the incoming POST data
            logger.debug("Received POST data: %s", request.POST)

            username = request.POST.get('username')
            password = request.POST.get('password')

            # Log the extracted values
            logger.debug("Username: %s, Password: %s", username, password)

            # Validate inputs
            if not username or not password:
                return JsonResponse({'success': False, 'error': 'All fields are required'})

            user = authenticate(request, username=username, password=password)

            if user is not None:
                try:
                    fit_user = Fit_Users.objects.get(user=user)
                    return JsonResponse({'success': True, 'message': 'Login successful'})
                except Fit_Users.DoesNotExist:
                    return JsonResponse({'success': False, 'error': 'User not found in FitSync'})
            else:
                return JsonResponse({'success': False, 'error': 'Invalid credentials'})
        except Exception as e:
            logger.error("Login error: %s", str(e))
            return JsonResponse({'success': False, 'error': str(e)})
    return JsonResponse({'success': False, 'error': 'Invalid request'})

@csrf_exempt
def api_products_view(request):
    if request.method == "GET":
        try:
            products = Product.objects.all()
            product_list = []
            for product in products:
                product_data = {
                    'id': product.id,
                    'name': product.name,
                    'description': product.description,
                    'price': float(product.price),  # Convert Decimal to float
                    'stock': product.stock,
                    'image': product.image.url if product.image else None,  # Full URL or null
                }
                product_list.append(product_data)
            return JsonResponse({'success': True, 'products': product_list})
        except Exception as e:
            logger.error("Products fetch error: %s", str(e))
            return JsonResponse({'success': False, 'error': str(e)})
    return JsonResponse({'success': False, 'error': 'Invalid request'})

@csrf_exempt
def api_add_to_cart_view(request):
    if request.method == "POST":
        logger.debug("Received POST data: %s", request.POST)
        try:
            username = request.POST.get('username')
            product_id = request.POST.get('product_id')
            quantity = int(request.POST.get('quantity', 1))

            if not username or not product_id:
                logger.error("Missing fields - Username: %s, Product ID: %s", username, product_id)
                return JsonResponse({'success': False, 'error': 'Username and product_id are required'})

            product = Product.objects.get(id=product_id)
            
            # Check stock availability
            existing_cart = Cart.objects.filter(username=username, product=product).first()
            current_quantity = existing_cart.quantity if existing_cart else 0
            requested_quantity = current_quantity + quantity

            if product.stock < requested_quantity:
                return JsonResponse({
                    'success': False,
                    'error': f'Insufficient stock. Available: {product.stock}, Requested: {requested_quantity}'
                })

            # Add or update cart
            cart_item, created = Cart.objects.get_or_create(
                username=username,
                product=product,
                defaults={'quantity': quantity}
            )
            if not created:
                cart_item.quantity += quantity
                cart_item.save()

            return JsonResponse({
                'success': True,
                'message': f'Added {quantity} {product.name}(s) to cart',
                'new_quantity': cart_item.quantity
            })
        except Product.DoesNotExist:
            return JsonResponse({'success': False, 'error': 'Product not found'})
        except Exception as e:
            logger.error("Add to cart error: %s", str(e))
            return JsonResponse({'success': False, 'error': str(e)})
    return JsonResponse({'success': False, 'error': 'Invalid request'})

@csrf_exempt
def api_cart_view(request):
    if request.method == "GET":
        try:
            username = request.GET.get('username')
            if not username:
                return JsonResponse({'success': False, 'error': 'Username is required'})

            cart_items = Cart.objects.filter(username=username)
            cart_list = []
            total_price = 0
            for item in cart_items:
                item_data = {
                    'id': item.id,
                    'product_id': item.product.id,
                    'name': item.product.name,
                    'price': float(item.product.price),
                    'quantity': item.quantity,
                    'image': item.product.image.url if item.product.image else None,
                }
                cart_list.append(item_data)
                total_price += float(item.product.price) * item.quantity
            return JsonResponse({
                'success': True,
                'cart': cart_list,
                'total_price': total_price
            })
        except Exception as e:
            logger.error("Cart fetch error: %s", str(e))
            return JsonResponse({'success': False, 'error': str(e)})
    return JsonResponse({'success': False, 'error': 'Invalid request'})

@csrf_exempt
def api_update_cart_view(request):
    if request.method == "POST":
        try:
            username = request.POST.get('username')
            cart_id = request.POST.get('cart_id')
            action = request.POST.get('action')

            if not all([username, cart_id, action]):
                return JsonResponse({'success': False, 'error': 'Username, cart_id, and action are required'})

            cart_item = Cart.objects.get(id=cart_id, username=username)
            product = cart_item.product

            if action == 'increment':
                if product.stock <= cart_item.quantity:
                    return JsonResponse({'success': False, 'error': 'Insufficient stock'})
                cart_item.quantity += 1
                cart_item.save()
            elif action == 'decrement':
                if cart_item.quantity > 1:  # Stop at 1
                    cart_item.quantity -= 1
                    cart_item.save()
            elif action == 'remove':
                cart_item.delete()
            else:
                return JsonResponse({'success': False, 'error': 'Invalid action'})

            return JsonResponse({'success': True, 'message': f'Cart updated: {action}'})
        except Cart.DoesNotExist:
            return JsonResponse({'success': False, 'error': 'Cart item not found'})
        except Exception as e:
            logger.error("Cart update error: %s", str(e))
            return JsonResponse({'success': False, 'error': str(e)})
    return JsonResponse({'success': False, 'error': 'Invalid request'})

from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from .models import Order, OrderItem, Cart, Product
import json
import logging

logger = logging.getLogger(__name__)

@csrf_exempt
def api_create_order_view(request):
    if request.method == "POST":
        try:
            username = request.POST.get('username')
            credit_card = request.POST.get('credit_card')
            cvv = request.POST.get('cvv')
            expiry_date = request.POST.get('expiry_date')
            location = request.POST.get('location')

            if not all([username, credit_card, cvv, expiry_date, location]):
                return JsonResponse({'success': False, 'error': 'All payment fields are required'})

            # Validate credit card (simple check for 16 digits)
            if not (credit_card.isdigit() and len(credit_card) == 16):
                return JsonResponse({'success': False, 'error': 'Invalid credit card number'})
            if not (cvv.isdigit() and len(cvv) == 3):
                return JsonResponse({'success': False, 'error': 'Invalid CVV'})
            # Add more validation for expiry_date if needed (e.g., MM/YY format)

            # Fetch cart items
            cart_items = Cart.objects.filter(username=username)
            if not cart_items:
                return JsonResponse({'success': False, 'error': 'Cart is empty'})

            # Check stock availability and reduce stock
            for item in cart_items:
                product = item.product
                if product.stock < item.quantity:
                    return JsonResponse({
                        'success': False,
                        'error': f'Insufficient stock for {product.name}. Available: {product.stock}, Requested: {item.quantity}'
                    })
                # Reduce stock
                product.stock -= item.quantity
                product.save()

            # Create order
            order = Order.objects.create(username=username)
            total_price = 0
            for item in cart_items:
                OrderItem.objects.create(
                    order=order,
                    product=item.product,
                    quantity=item.quantity
                )
                total_price += float(item.product.price) * item.quantity
            order.total_price = total_price
            order.location = location
            order.save()

            # Clear cart
            cart_items.delete()

            return JsonResponse({
                'success': True,
                'message': 'Order placed successfully',
                'order_id': order.id
            })
        except Exception as e:
            logger.error("Order creation error: %s", str(e))
            return JsonResponse({'success': False, 'error': str(e)})
    return JsonResponse({'success': False, 'error': 'Invalid request'})
@csrf_exempt
def api_orders_view(request):
    if request.method == "GET":
        try:
            username = request.GET.get('username')
            if not username:
                return JsonResponse({'success': False, 'error': 'Username is required'})

            orders = Order.objects.filter(username=username).order_by('-added_at')
            orders_list = []
            for order in orders:
                items = OrderItem.objects.filter(order=order)
                order_items = [
                    {
                        'product_id': item.product.id,
                        'name': item.product.name,
                        'price': float(item.product.price),
                        'quantity': item.quantity,
                        'image': item.product.image.url if item.product.image else None
                    } for item in items
                ]
                orders_list.append({
                    'order_id': order.id,
                    'total_price': float(order.total_price),
                    'added_at': order.added_at.isoformat(),
                    'status': order.status,
                    'location': order.location,
                    'items': order_items
                })
            return JsonResponse({
                'success': True,
                'orders': orders_list
            })
        except Exception as e:
            logger.error("Orders fetch error: %s", str(e))
            return JsonResponse({'success': False, 'error': str(e)})
    return JsonResponse({'success': False, 'error': 'Invalid request'})

@csrf_exempt
def api_add_food_log(request):
    if request.method == "POST":
        try:
            data = json.loads(request.body)
            username = data.get('username')
            date = data.get('date')  # Expected format: YYYY-MM-DD
            added_details = data.get('added_details')
            comments = data.get('comments', [])  # Expected as a list

            if not all([username, date, added_details]):
                return JsonResponse({'success': False, 'error': 'Missing required fields'})

            food_log = DailyFood(
                username=username,
                date=date,
                added_details=added_details,
                comments=comments
            )
            food_log.save()
            return JsonResponse({
                'success': True,
                'message': 'Food log added successfully'
            })
        except Exception as e:
            logger.error("Add food log error: %s", str(e))
            return JsonResponse({'success': False, 'error': str(e)})
    return JsonResponse({'success': False, 'error': 'Invalid request'})

@csrf_exempt
def api_food_history(request):
    if request.method == "GET":
        try:
            username = request.GET.get('username')
            if not username:
                return JsonResponse({'success': False, 'error': 'Username is required'})

            food_logs = DailyFood.objects.filter(username=username).order_by('-date')
            history = [
                {
                    'id': log.id,
                    'date': log.date.isoformat(),
                    'added_details': log.added_details,
                    'comments': log.comments,
                    'created_at': log.created_at.strftime('%Y-%m-%dT%H:%M:%SZ')  # Simplified format: 2025-04-02T16:33:23Z
                }
                for log in food_logs
            ]
            return JsonResponse({
                'success': True,
                'history': history
            })
        except Exception as e:
            logger.error("Food history fetch error: %s", str(e))
            return JsonResponse({'success': False, 'error': str(e)})
    return JsonResponse({'success': False, 'error': 'Invalid request'})

@csrf_exempt
def api_get_trainers(request):
    if request.method == "GET":
        try:
            trainers = Trainer.objects.filter(status=True)  # Only active trainers
            trainer_list = [
                {
                    'name': trainer.name,
                    'email': trainer.email,
                    'speciality': trainer.specialty,
                }
                for trainer in trainers
            ]
            return JsonResponse({
                'success': True,
                'trainers': trainer_list
            })
        except Exception as e:
            logger.error("Get trainers error: %s", str(e))
            return JsonResponse({'success': False, 'error': str(e)})
    return JsonResponse({'success': False, 'error': 'Invalid request'})

@csrf_exempt
def api_create_booking(request):
    if request.method == "POST":
        try:
            data = json.loads(request.body)
            username = data.get('username')
            trainer_name = data.get('trainer_name')
            trainer_email = data.get('trainer_email')
            speciality = data.get('speciality')

            if not all([username, trainer_name, trainer_email, speciality]):
                return JsonResponse({'success': False, 'error': 'Missing required fields'})

            # Check if the user already has a pending or approved booking
            existing_booking = Booking.objects.filter(
                username=username,
                status__in=['Pending', 'Approved']
            ).exists()
            if existing_booking:
                return JsonResponse({
                    'success': False,
                    'error': 'You already have a booking'
                })

            booking = Booking(
                username=username,
                trainer_name=trainer_name,
                trainer_email=trainer_email,
                speciality=speciality,
                status='Pending'
            )
            booking.save()
            return JsonResponse({
                'success': True,
                'message': 'Booking created successfully'
            })
        except Exception as e:
            logger.error("Create booking error: %s", str(e))
            return JsonResponse({'success': False, 'error': str(e)})
    return JsonResponse({'success': False, 'error': 'Invalid request'})

@csrf_exempt
def api_check_booking(request):
    if request.method == "GET":
        try:
            username = request.GET.get('username')
            if not username:
                return JsonResponse({'success': False, 'error': 'Username is required'})

            booking = Booking.objects.filter(
                username=username,
                status__in=['Pending', 'Approved']
            ).first()
            if booking:
                return JsonResponse({
                    'success': True,
                    'has_booking': True,
                    'booking': {
                        'trainer_name': booking.trainer_name,
                        'trainer_email': booking.trainer_email,
                        'speciality': booking.speciality,
                        'status': booking.status
                    }
                })
            return JsonResponse({
                'success': True,
                'has_booking': False
            })
        except Exception as e:
            logger.error("Check booking error: %s", str(e))
            return JsonResponse({'success': False, 'error': str(e)})
    return JsonResponse({'success': False, 'error': 'Invalid request'})

@csrf_exempt
def api_delete_booking(request):
    logger.debug("Received DELETE booking request: Method=%s, Body=%s", request.method, request.body)
    if request.method == "POST":
        try:
            data = json.loads(request.body)
            username = data.get('username')
            logger.debug("Username for delete booking: %s", username)

            if not username:
                logger.warning("Delete booking failed: Username is required")
                return JsonResponse({'success': False, 'error': 'Username is required'})

            booking = Booking.objects.filter(
                username=username,
                status__in=['Pending', 'Approved']
            ).first()
            if not booking:
                logger.warning("Delete booking failed: No booking found for username %s", username)
                return JsonResponse({'success': False, 'error': 'No booking found'})

            booking.delete()
            logger.info("Booking removed successfully for username: %s", username)
            return JsonResponse({
                'success': True,
                'message': 'Booking removed successfully'
            })
        except json.JSONDecodeError as e:
            logger.error("Delete booking JSON decode error: %s", str(e))
            return JsonResponse({'success': False, 'error': 'Invalid JSON format'})
        except Exception as e:
            logger.error("Delete booking error: %s", str(e))
            return JsonResponse({'success': False, 'error': str(e)})
    logger.warning("Delete booking failed: Invalid request method: %s", request.method)
    return JsonResponse({'success': False, 'error': 'Invalid request'})


#####################################################################################

def orders_view(request):
    if not request.user.is_authenticated or not request.user.is_superuser:
        return redirect('login')
    
    status_filter = request.GET.get('status', 'All')
    
    # Filter orders based on status
    if status_filter == 'All':
        orders = Order.objects.all()
    else:
        orders = Order.objects.filter(status=status_filter)
    
    if request.method == "POST":
        action = request.POST.get('action')
        order_id = request.POST.get('order_id')
        
        try:
            order = Order.objects.get(id=order_id)
            if action == 'update_status':
                new_status = request.POST.get('status')
                order.status = new_status
                order.save()
                return JsonResponse({'success': True, 'message': 'Order status updated successfully!'})
        except Exception as e:
            return JsonResponse({'success': False, 'error': str(e)})
    
    context = {
        'orders': orders,
        'status_filter': status_filter,
    }
    return render(request, 'orders.html', context)

def users_view(request):
    if not request.user.is_authenticated or not request.user.is_superuser:
        return redirect('login')
    
    users = Fit_Users.objects.all()
    
    if request.method == "POST":
        action = request.POST.get('action')
        
        if action == 'mark_attendance':
            user_id = request.POST.get('user_id')
            status = request.POST.get('status')
            today = datetime.today().date()
            
            try:
                user = Fit_Users.objects.get(id=user_id)
                # Check if attendance is already marked for today
                if Attendance.objects.filter(user=user, date=today).exists():
                    return JsonResponse({'success': False, 'error': 'Attendance already marked for today!'})
                
                # Mark attendance
                Attendance.objects.create(user=user, date=today, status=status)
                return JsonResponse({'success': True, 'message': 'Attendance marked successfully!'})
            except Exception as e:
                return JsonResponse({'success': False, 'error': str(e)})
        
        elif action == 'view_history':
            user_id = request.POST.get('user_id')
            filter_type = request.POST.get('filter_type', 'all')
            filter_value = request.POST.get('filter_value', '')
            
            try:
                user = Fit_Users.objects.get(id=user_id)
                attendance_records = Attendance.objects.filter(user=user)
                
                # Apply filters
                if filter_type == 'date' and filter_value:
                    attendance_records = attendance_records.filter(date=filter_value)
                elif filter_type == 'month' and filter_value:
                    year, month = map(int, filter_value.split('-'))
                    attendance_records = attendance_records.filter(date__year=year, date__month=month)
                elif filter_type == 'year' and filter_value:
                    attendance_records = attendance_records.filter(date__year=filter_value)
                
                # Prepare history data for the response
                history_data = [
                    {'date': record.date.strftime('%Y-%m-%d'), 'status': record.status}
                    for record in attendance_records
                ]
                return JsonResponse({'success': True, 'history': history_data})
            except Exception as e:
                return JsonResponse({'success': False, 'error': str(e)})
        
        elif action == 'send_email':
            subject = request.POST.get('subject')
            body = request.POST.get('body')
            
            try:
                # Get all user emails
                user_emails = Fit_Users.objects.values_list('email', flat=True)
                if not user_emails:
                    return JsonResponse({'success': False, 'error': 'No users found to send email to.'})
                
                # Send email to all users
                send_mail(
                    subject=subject,
                    message=body,
                    from_email='leopaulose.works@gmail.com',
                    recipient_list=list(user_emails),
                    fail_silently=False,
                )
                return JsonResponse({'success': True, 'message': 'Email sent successfully to all users!'})
            except Exception as e:
                return JsonResponse({'success': False, 'error': f'Failed to send email: {str(e)}'})
    
    context = {'users': users}
    return render(request, 'users.html', context)

########################################################################################################
def view_bookings(request):
    if not request.user.is_authenticated:
        return redirect('login')
    
    try:
        trainer_email = request.session.get('trainer_email')
        if not trainer_email:
            return redirect('login')
        
        # Fetch bookings for the trainer
        bookings = Booking.objects.filter(trainer_email=trainer_email)
        
        if request.method == "POST":
            action = request.POST.get('action')
            if action == 'update_status':
                booking_id = request.POST.get('booking_id')
                new_status = request.POST.get('status')
                
                try:
                    booking = Booking.objects.get(id=booking_id, trainer_email=trainer_email)
                    booking.status = new_status
                    booking.save()
                    return JsonResponse({'success': True, 'message': 'Booking status updated successfully!'})
                except Booking.DoesNotExist:
                    return JsonResponse({'success': False, 'error': 'Booking not found.'})
                except Exception as e:
                    return JsonResponse({'success': False, 'error': str(e)})
        
        context = {
            'bookings': bookings,
            'trainer': Trainer.objects.get(email=trainer_email)
        }
        return render(request, 'view_bookings.html', context)
    
    except Trainer.DoesNotExist:
        return redirect('login')

def daily_food_view(request):
    if not request.user.is_authenticated:
        return redirect('login')
    
    try:
        trainer_email = request.session.get('trainer_email')
        if not trainer_email:
            return redirect('login')
        
        # Fetch trainer
        trainer = Trainer.objects.get(email=trainer_email)
        
        # Fetch usernames of users who have bookings with this trainer
        booked_usernames = Booking.objects.filter(trainer_email=trainer_email).values_list('username', flat=True)
        
        # Fetch DailyFood entries for those usernames
        daily_foods = DailyFood.objects.filter(username__in=booked_usernames)
        
        if request.method == "POST":
            action = request.POST.get('action')
            if action == 'update_comments':
                daily_food_id = request.POST.get('daily_food_id')
                new_comment = request.POST.get('comment')
                
                try:
                    daily_food = DailyFood.objects.get(id=daily_food_id, username__in=booked_usernames)
                    # Append the new comment to the existing list
                    comments = daily_food.comments
                    comments.append(new_comment)
                    daily_food.comments = comments
                    daily_food.save()
                    return JsonResponse({'success': True, 'message': 'Comment added successfully!', 'comments': comments})
                except DailyFood.DoesNotExist:
                    return JsonResponse({'success': False, 'error': 'Daily food entry not found.'})
                except Exception as e:
                    return JsonResponse({'success': False, 'error': str(e)})
            
            elif action == 'edit_comment':
                daily_food_id = request.POST.get('daily_food_id')
                comment_index = int(request.POST.get('comment_index'))
                updated_comment = request.POST.get('comment')
                
                try:
                    daily_food = DailyFood.objects.get(id=daily_food_id, username__in=booked_usernames)
                    comments = daily_food.comments
                    if 0 <= comment_index < len(comments):
                        comments[comment_index] = updated_comment
                        daily_food.comments = comments
                        daily_food.save()
                        return JsonResponse({'success': True, 'message': 'Comment updated successfully!', 'comments': comments})
                    else:
                        return JsonResponse({'success': False, 'error': 'Invalid comment index.'})
                except DailyFood.DoesNotExist:
                    return JsonResponse({'success': False, 'error': 'Daily food entry not found.'})
                except Exception as e:
                    return JsonResponse({'success': False, 'error': str(e)})
        
        context = {
            'daily_foods': daily_foods,
            'trainer': trainer
        }
        return render(request, 'daily_food.html', context)
    
    except Trainer.DoesNotExist:
        return redirect('login')

def upload_video_view(request):
    if not request.user.is_authenticated:
        return redirect('login')
    
    try:
        trainer_email = request.session.get('trainer_email')
        if not trainer_email:
            return redirect('login')
        
        trainer = Trainer.objects.get(email=trainer_email)
        
        if request.method == "POST":
            details = request.POST.get('details')
            video = request.FILES.get('video')
            
            if not details or not video:
                return render(request, 'upload_video.html', {
                    'trainer': trainer,
                    'error': 'Please provide both details and a video file.'
                })
            
            try:
                # Save the video
                TrainerVideo.objects.create(
                    trainer_email=trainer_email,
                    details=details,
                    video=video
                )
                return render(request, 'upload_video.html', {
                    'trainer': trainer,
                    'success': 'Video uploaded successfully!'
                })
            except Exception as e:
                return render(request, 'upload_video.html', {
                    'trainer': trainer,
                    'error': f'Failed to upload video: {str(e)}'
                })
        
        context = {
            'trainer': trainer
        }
        return render(request, 'upload_video.html', context)
    
    except Trainer.DoesNotExist:
        return redirect('login')

def manage_videos_view(request):
    if not request.user.is_authenticated:
        return redirect('login')
    
    try:
        trainer_email = request.session.get('trainer_email')
        if not trainer_email:
            return redirect('login')
        
        trainer = Trainer.objects.get(email=trainer_email)
        videos = TrainerVideo.objects.filter(trainer_email=trainer_email)
        
        if request.method == "POST":
            action = request.POST.get('action')
            
            if action == 'update_details':
                video_id = request.POST.get('video_id')
                new_details = request.POST.get('details')
                
                try:
                    video = TrainerVideo.objects.get(id=video_id, trainer_email=trainer_email)
                    video.details = new_details
                    video.save()
                    return JsonResponse({'success': True, 'message': 'Video details updated successfully!'})
                except TrainerVideo.DoesNotExist:
                    return JsonResponse({'success': False, 'error': 'Video not found.'})
                except Exception as e:
                    return JsonResponse({'success': False, 'error': str(e)})
            
            elif action == 'update_video':
                video_id = request.POST.get('video_id')
                new_video = request.FILES.get('video')
                
                if not new_video:
                    return JsonResponse({'success': False, 'error': 'Please select a video file.'})
                
                try:
                    video = TrainerVideo.objects.get(id=video_id, trainer_email=trainer_email)
                    # Delete the old video file from storage
                    if video.video:
                        video.video.delete(save=False)
                    video.video = new_video
                    video.save()
                    return JsonResponse({'success': True, 'message': 'Video file updated successfully!'})
                except TrainerVideo.DoesNotExist:
                    return JsonResponse({'success': False, 'error': 'Video not found.'})
                except Exception as e:
                    return JsonResponse({'success': False, 'error': str(e)})
        
        context = {
            'trainer': trainer,
            'videos': videos
        }
        return render(request, 'manage_videos.html', context)
    
    except Trainer.DoesNotExist:
        return redirect('login')

@csrf_exempt
def api_get_trainer_videos(request):
    if request.method == "GET":
        try:
            trainer_email = request.GET.get('trainer_email')
            if not trainer_email:
                return JsonResponse({'success': False, 'error': 'Trainer email is required'})

            videos = TrainerVideo.objects.filter(trainer_email=trainer_email)
            video_list = [
                {
                    'details': video.details,
                    'video_url': video.video.url,  # Returns the URL to the video file
                    'created_at': video.created_at.strftime('%Y-%m-%d %H:%M:%S'),
                }
                for video in videos
            ]
            return JsonResponse({
                'success': True,
                'videos': video_list
            })
        except Exception as e:
            logger.error("Get trainer videos error: %s", str(e))
            return JsonResponse({'success': False, 'error': str(e)})
    return JsonResponse({'success': False, 'error': 'Invalid request'})

@csrf_exempt
def api_get_review(request):
    if request.method == "GET":
        try:
            username = request.GET.get('username')
            trainer_email = request.GET.get('trainer_email')
            if not username or not trainer_email:
                return JsonResponse({'success': False, 'error': 'Username and trainer email are required'})

            review = Reviews.objects.filter(username=username, trainer_email=trainer_email).first()
            if review:
                return JsonResponse({
                    'success': True,
                    'has_review': True,
                    'review': {
                        'rating': review.rating,
                        'comment': review.comment,
                        'created_at': review.created_at.strftime('%Y-%m-%d %H:%M:%S'),
                        'updated_at': review.updated_at.strftime('%Y-%m-%d %H:%M:%S'),
                    }
                })
            return JsonResponse({
                'success': True,
                'has_review': False
            })
        except Exception as e:
            logger.error("Get review error: %s", str(e))
            return JsonResponse({'success': False, 'error': str(e)})
    return JsonResponse({'success': False, 'error': 'Invalid request'})

@csrf_exempt
def api_create_review(request):
    if request.method == "POST":
        try:
            data = json.loads(request.body)
            username = data.get('username')
            trainer_email = data.get('trainer_email')
            rating = data.get('rating')
            comment = data.get('comment')

            if not all([username, trainer_email, rating, comment]):
                return JsonResponse({'success': False, 'error': 'Missing required fields'})

            if not (1 <= int(rating) <= 5):
                return JsonResponse({'success': False, 'error': 'Rating must be between 1 and 5'})

            # Check if a review already exists
            if Reviews.objects.filter(username=username, trainer_email=trainer_email).exists():
                return JsonResponse({'success': False, 'error': 'Review already exists. Use update instead.'})

            review = Reviews(
                username=username,
                trainer_email=trainer_email,
                rating=rating,
                comment=comment
            )
            review.save()
            return JsonResponse({
                'success': True,
                'message': 'Review created successfully'
            })
        except Exception as e:
            logger.error("Create review error: %s", str(e))
            return JsonResponse({'success': False, 'error': str(e)})
    return JsonResponse({'success': False, 'error': 'Invalid request'})

@csrf_exempt
def api_update_review(request):
    if request.method == "POST":
        try:
            data = json.loads(request.body)
            username = data.get('username')
            trainer_email = data.get('trainer_email')
            rating = data.get('rating')
            comment = data.get('comment')

            if not all([username, trainer_email, rating, comment]):
                return JsonResponse({'success': False, 'error': 'Missing required fields'})

            if not (1 <= int(rating) <= 5):
                return JsonResponse({'success': False, 'error': 'Rating must be between 1 and 5'})

            review = Reviews.objects.filter(username=username, trainer_email=trainer_email).first()
            if not review:
                return JsonResponse({'success': False, 'error': 'Review not found. Create a new review first.'})

            review.rating = rating
            review.comment = comment
            review.save()
            return JsonResponse({
                'success': True,
                'message': 'Review updated successfully'
            })
        except Exception as e:
            logger.error("Update review error: %s", str(e))
            return JsonResponse({'success': False, 'error': str(e)})
    return JsonResponse({'success': False, 'error': 'Invalid request'})

def review_admin_view(request):
    if not request.user.is_authenticated:
        return redirect('login')
    
    # Fetch all reviews
    reviews = Reviews.objects.all()
    
    context = {
        'reviews': reviews
    }
    return render(request, 'review_admin.html', context)

def review_trainer_view(request):
    if not request.user.is_authenticated:
        return redirect('login')
    
    try:
        trainer_email = request.session.get('trainer_email')
        if not trainer_email:
            return redirect('login')
        
        trainer = Trainer.objects.get(email=trainer_email)
        reviews = Reviews.objects.filter(trainer_email=trainer_email)
        
        context = {
            'trainer': trainer,
            'reviews': reviews
        }
        return render(request, 'review_trainer.html', context)
    
    except Trainer.DoesNotExist:
        return redirect('login')